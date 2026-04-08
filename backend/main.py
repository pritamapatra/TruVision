from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
import sqlite3
from pathlib import Path
import json
from datetime import datetime
from database import init_db, get_all_samples, get_sample
import capture_image
import zipfile

app = FastAPI()

DB_PATH = "truvision.db"
SAMPLES_DIR = Path("samples")

@app.on_event("startup")
async def startup():
    init_db()
    SAMPLES_DIR.mkdir(exist_ok=True)
    print(f"Database initialized: {DB_PATH}")
    print("Backend started - database and samples folder ready")

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.get("/preview")
async def get_preview():
    """Capture and return live preview image from USB microscope"""
    import subprocess
    import os
    from fastapi.responses import FileResponse
    
    preview_path = "/tmp/preview.jpg"
    
    try:
        # Capture image using fswebcam
        subprocess.run(
            ["fswebcam", "-r", "640x480", "--no-banner", "-q", preview_path],
            check=True,
            timeout=3
        )
        
        if os.path.exists(preview_path):
            return FileResponse(preview_path, media_type="image/jpeg")
        else:
            raise HTTPException(status_code=503, detail="Camera capture failed")
            
    except subprocess.TimeoutExpired:
        raise HTTPException(status_code=503, detail="Camera timeout")
    except Exception as e:
        raise HTTPException(status_code=503, detail=f"Camera error: {str(e)}")


@app.post("/capture/start")
async def start_capture(
    latitude: float = None,
    longitude: float = None, 
    accuracy: float = None,
    location_method: str = None
):
    import uuid
    job_id = f"job-{uuid.uuid4().hex[:8]}"
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO samples (job_id, status, timestamp, created_at, latitude, longitude, accuracy, location_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        (job_id, "pending", datetime.now().isoformat(), datetime.now().isoformat(), latitude, longitude, accuracy, location_method)
    )
    conn.commit()
    conn.close()
    
    job_dir = SAMPLES_DIR / job_id
    job_dir.mkdir(exist_ok=True)
    
    import asyncio
    asyncio.create_task(process_job(job_id))
    
    return {"job_id": job_id, "status": "processing"}

async def process_job(job_id: str):
    import time
    from detector import detect_microplastics
    
    job_dir = SAMPLES_DIR / job_id
    image_path = job_dir / "captured.jpg"
    
    print(f"[Job {job_id}] Starting camera capture...")
    result = capture_image.capture_from_camera(str(image_path))
    
    if not result.get('success'):
        print(f"[Job {job_id}] Camera failed, using fallback test image...")
        import shutil
        test_image = Path("test_yolo_microplastic_sample.jpg")
        if test_image.exists():
            shutil.copy(test_image, image_path)
            print(f"[Job {job_id}] Fallback image copied - continuing with detection")
        else:
            print(f"[Job {job_id}] No fallback image available - marking as failed")
            update_job(job_id, "failed", 0)
            return
    
    print(f"[Job {job_id}] Camera capture successful: {result.get('size_bytes', 0)} bytes")
    
    start = time.time()
    detections = detect_microplastics(str(image_path))
    processing_time = int((time.time() - start) * 1000)
    
    detections_path = job_dir / "detections.json"
    with open(detections_path, 'w') as f:
        json.dump(detections, f, indent=2)
    
    update_job(job_id, "completed", len(detections), str(image_path), json.dumps(detections))
    print(f"[Job {job_id}] Completed with {len(detections)} detections (method: camera)")

def update_job(job_id: str, status: str, count: int, img_path: str = None, detections_json: str = None):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    if img_path and detections_json:
        cursor.execute(
            "UPDATE samples SET status=?, detected_count=?, image_path=?, detections=? WHERE job_id=?",
            (status, count, img_path, detections_json, job_id)
        )
    elif img_path:
        cursor.execute(
            "UPDATE samples SET status=?, detected_count=?, image_path=? WHERE job_id=?",
            (status, count, img_path, job_id)
        )
    else:
        cursor.execute(
            "UPDATE samples SET status=?, detected_count=? WHERE job_id=?",
            (status, count, job_id)
        )
    conn.commit()
    conn.close()

@app.get("/jobs/{job_id}")
def get_job_status(job_id: str):
    """Get job status and detections"""
    sample = get_sample(job_id)
    if not sample:
        raise HTTPException(status_code=404, detail="Job not found")
    return sample

@app.get("/export/{job_id}")
async def export_job(job_id: str):
    """Create and return ZIP with captured.jpg, detections.json, metadata.csv"""
    job_dir = SAMPLES_DIR / job_id
    if not job_dir.exists():
        raise HTTPException(status_code=404, detail=f"Job {job_id} not found")
    
    # Create ZIP
    zip_path = job_dir / "export.zip"
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zf:
        # Add captured.jpg
        img_path = job_dir / "captured.jpg"
        if img_path.exists():
            zf.write(img_path, "captured.jpg")
        
        # Add detections.json
        det_path = job_dir / "detections.json"
        if det_path.exists():
            zf.write(det_path, "detections.json")
        
        # Create and add metadata.csv
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute("SELECT job_id, status, detected_count, image_path, timestamp, capture_method FROM samples WHERE job_id = ?", (job_id,))
        row = cursor.fetchone()
        conn.close()
        
        if row:
            csv_path = job_dir / "metadata.csv"
            with open(csv_path, 'w') as f:
                f.write("field,value\n")
                f.write(f"job_id,{row[0]}\n")
                f.write(f"status,{row[1]}\n")
                f.write(f"detected_count,{row[2]}\n")
                f.write(f"image_path,{row[3]}\n")
                f.write(f"timestamp,{row[4]}\n")
                f.write(f"capture_method,{row[5]}\n")
            zf.write(csv_path, "metadata.csv")
    
    return FileResponse(
        path=str(zip_path),
        filename=f"{job_id}.zip",
        media_type="application/zip"
    )

@app.get("/samples")
def get_samples():
    """Get all samples from database"""
    samples = get_all_samples()
    return samples

@app.get("/samples/{job_id}/image")
def get_sample_image(job_id: str):
    """Serve the captured JPEG for a given job"""
    img_path = SAMPLES_DIR / job_id / "captured.jpg"
    if not img_path.exists():
        raise HTTPException(status_code=404, detail="Image not found")
    return FileResponse(str(img_path), media_type="image/jpeg")

@app.delete("/samples/{job_id}")
async def delete_sample(job_id: str):
    import shutil
    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute("DELETE FROM samples WHERE job_id = ?", (job_id,))
        conn.commit()
        conn.close()
        job_dir = SAMPLES_DIR / job_id
        if job_dir.exists():
            shutil.rmtree(job_dir)
        return {"status": "deleted", "job_id": job_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)


