from fastapi import FastAPI

app = FastAPI()

@app.get("/health")
def health():
    return {"status": "ok"}

# Week 3 Placeholder Endpoints

@app.get("/samples")
def get_samples():
    """Returns empty list for now - will contain sample history later"""
    return []

@app.post("/capture/start")
def start_capture():
    """Placeholder for starting capture job"""
    return {
        "job_id": "test-001",
        "status": "running"
    }

@app.get("/jobs/{job_id}")
def get_job_status(job_id: str):
    """Placeholder for job status polling"""
    return {
        "job_id": job_id,
        "status": "completed",
        "detected_count": 0
    }
