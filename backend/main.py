from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import asyncio
import uuid
import os
from datetime import datetime
from database import init_db, save_sample, update_sample_status, get_all_samples, get_sample
from yolo_detector import detect_microplastics
from capture_image import capture_from_camera

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def startup_event():
    init_db()
    os.makedirs("samples", exist_ok=True)
    print("Backend started - database and samples folder ready")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/samples")
def get_samples():
    samples = get_all_samples()
    return samples

@app.post("/capture/start")
async def start_capture():
    job_id = f"job-{uuid.uuid4().hex[:8]}"
    save_sample(job_id, status="pending")
    
    sample_dir = f"samples/{job_id}"
    os.makedirs(sample_dir, exist_ok=True)
    
    asyncio.create_task(simulate_processing(job_id, sample_dir))
    
    return {"job_id": job_id, "status": "pending"}

@app.get("/jobs/{job_id}")
def get_job_status(job_id: str):
    sample = get_sample(job_id)
    if not sample:
        raise HTTPException(status_code=404, detail="Job not found")
    
    return {
        "job_id": sample["job_id"],
        "status": sample["status"],
        "detected_count": sample["detected_count"],
        "image_path": sample["image_path"],
        "detections": sample.get("detections"),
        "capture_method": sample.get("capture_method")
    }

async def simulate_processing(job_id: str, sample_dir: str):
    await asyncio.sleep(2)
    update_sample_status(job_id, "running")
    
    image_path = f"{sample_dir}/captured.jpg"
    
    # REAL CAMERA CAPTURE - Replace placeholder with actual camera
    print(f"[Job {job_id}] Starting camera capture...")
    capture_result = capture_from_camera(image_path)
    
    if capture_result["success"]:
        print(f"[Job {job_id}] Camera capture successful: {capture_result['file_size']} bytes")
        capture_timestamp = capture_result["timestamp"]
        capture_method = "camera"
    else:
        # Fallback to test image if camera fails
        print(f"[Job {job_id}] Camera failed: {capture_result.get('error')}, using test image")
        test_image = "/home/truvision/test_yolo/microplastic_sample.jpg"
        if os.path.exists(test_image):
            import shutil
            shutil.copy(test_image, image_path)
            capture_timestamp = datetime.now().isoformat()
            capture_method = "test_image"
            print(f"[Job {job_id}] Copied test image to {image_path}")
        else:
            print(f"[Job {job_id}] No test image available")
            capture_timestamp = None
            capture_method = None
    
    await asyncio.sleep(1)
    
    # Run YOLO detection
    detection_result = detect_microplastics(image_path) if os.path.exists(image_path) else {"detected_count": 0, "detections": []}
    
    # Update database with all metadata
    update_sample_status(
        job_id,
        "completed",
        detected_count=detection_result["detected_count"],
        image_path=image_path if os.path.exists(image_path) else None,
        detections=detection_result["detections"],
        capture_timestamp=capture_timestamp,
        capture_method=capture_method
    )
    
    print(f"[Job {job_id}] Completed with {detection_result['detected_count']} detections (method: {capture_method})")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
