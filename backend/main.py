from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import asyncio
import uuid
import os
from datetime import datetime
from database import init_db, save_sample, update_sample_status, get_all_samples, get_sample

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
    """Initialize database on startup"""
    init_db()
    os.makedirs("samples", exist_ok=True)
    print("Backend started - database and samples folder ready")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/samples")
def get_samples():
    """Returns real sample data from database"""
    samples = get_all_samples()
    return samples

@app.post("/capture/start")
async def start_capture():
    """Start a capture job - save metadata and simulate processing"""
    job_id = f"job-{uuid.uuid4().hex[:8]}"
    
    save_sample(job_id, status="pending")
    
    sample_dir = f"samples/{job_id}"
    os.makedirs(sample_dir, exist_ok=True)
    
    asyncio.create_task(simulate_processing(job_id))
    
    return {
        "job_id": job_id,
        "status": "pending"
    }

@app.get("/jobs/{job_id}")
def get_job_status(job_id: str):
    """Get job status with realistic state transitions"""
    sample = get_sample(job_id)
    
    if not sample:
        raise HTTPException(status_code=404, detail="Job not found")
    
    return {
        "job_id": sample["job_id"],
        "status": sample["status"],
        "detected_count": sample["detected_count"]
    }

async def simulate_processing(job_id: str):
    """Simulate job processing: pending -> running -> completed"""
    await asyncio.sleep(2)
    update_sample_status(job_id, "running")
    
    await asyncio.sleep(5)
    detected_count = 3
    update_sample_status(job_id, "completed", detected_count)
    
    print(f"Job {job_id} completed with {detected_count} detections")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
