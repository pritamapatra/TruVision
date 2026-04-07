import sqlite3
import os
import json
from datetime import datetime
from typing import List, Dict, Optional

DB_FILE = "truvision.db"

def init_db():
    """Initialize the database with samples table"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS samples (
            job_id TEXT PRIMARY KEY,
            timestamp TEXT NOT NULL,
            status TEXT NOT NULL,
            detected_count INTEGER,
            image_path TEXT,
            detections TEXT,
            capture_timestamp TEXT,
            capture_method TEXT,
            created_at TEXT NOT NULL,
            latitude REAL,
            longitude REAL,
            accuracy REAL,
            location_method TEXT
        )
    ''')
    conn.commit()
    conn.close()
    print(f"Database initialized: {DB_FILE}")

def save_sample(job_id: str, status: str = "pending", latitude: Optional[float] = None, 
                longitude: Optional[float] = None, accuracy: Optional[float] = None, 
                location_method: Optional[str] = None):
    """Save a new sample to the database"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    timestamp = datetime.now().isoformat()
    cursor.execute('''
        INSERT INTO samples (job_id, timestamp, status, detected_count, image_path, detections, capture_timestamp, capture_method, created_at, latitude, longitude, accuracy, location_method)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', (job_id, timestamp, status, None, None, None, None, None, timestamp, latitude, longitude, accuracy, location_method))
    conn.commit()
    conn.close()

def update_sample_status(job_id: str, status: str, detected_count: Optional[int] = None, 
                        image_path: Optional[str] = None, detections: Optional[List[Dict]] = None,
                        capture_timestamp: Optional[str] = None, capture_method: Optional[str] = None):
    """Update sample status and detection data"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    detections_json = json.dumps(detections) if detections else None
    
    cursor.execute('''
        UPDATE samples 
        SET status = ?, detected_count = ?, image_path = ?, detections = ?, capture_timestamp = ?, capture_method = ?
        WHERE job_id = ?
    ''', (status, detected_count, image_path, detections_json, capture_timestamp, capture_method, job_id))
    conn.commit()
    conn.close()

def get_all_samples() -> List[Dict]:
    """Get all samples from the database"""
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    cursor.execute('SELECT job_id, timestamp, status, detected_count, image_path, capture_method, latitude, longitude, accuracy, location_method FROM samples ORDER BY created_at DESC')
    rows = cursor.fetchall()
    conn.close()
    return [dict(row) for row in rows]

def get_sample(job_id: str) -> Optional[Dict]:
    """Get a specific sample by job_id"""
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    cursor.execute('SELECT job_id, timestamp, status, detected_count, image_path, detections, capture_timestamp, capture_method, latitude, longitude, accuracy, location_method FROM samples WHERE job_id = ?', (job_id,))
    row = cursor.fetchone()
    conn.close()
    if row:
        result = dict(row)
        if result.get('detections'):
            result['detections'] = json.loads(result['detections'])
        return result
    return None
