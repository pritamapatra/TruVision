import sqlite3
import os
from datetime import datetime
from typing import List, Dict, Optional

DB_FILE = "truvision.db"

def init_db():
    """Initialize the database with samples table"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS samples (
            job_id TEXT PRIMARY KEY,
            timestamp TEXT NOT NULL,
            status TEXT NOT NULL,
            detected_count INTEGER,
            created_at TEXT NOT NULL
        )
    """)
    
    conn.commit()
    conn.close()
    print(f"Database initialized: {DB_FILE}")

def save_sample(job_id: str, status: str = "pending"):
    """Save a new sample to the database"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    timestamp = datetime.now().isoformat()
    
    cursor.execute("""
        INSERT INTO samples (job_id, timestamp, status, detected_count, created_at)
        VALUES (?, ?, ?, ?, ?)
    """, (job_id, timestamp, status, None, timestamp))
    
    conn.commit()
    conn.close()

def update_sample_status(job_id: str, status: str, detected_count: Optional[int] = None):
    """Update sample status and detected count"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    if detected_count is not None:
        cursor.execute("""
            UPDATE samples 
            SET status = ?, detected_count = ?
            WHERE job_id = ?
        """, (status, detected_count, job_id))
    else:
        cursor.execute("""
            UPDATE samples 
            SET status = ?
            WHERE job_id = ?
        """, (status, job_id))
    
    conn.commit()
    conn.close()

def get_all_samples() -> List[Dict]:
    """Get all samples from the database"""
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT job_id, timestamp, status, detected_count
        FROM samples
        ORDER BY created_at DESC
    """)
    
    rows = cursor.fetchall()
    conn.close()
    
    return [dict(row) for row in rows]

def get_sample(job_id: str) -> Optional[Dict]:
    """Get a specific sample by job_id"""
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT job_id, timestamp, status, detected_count
        FROM samples
        WHERE job_id = ?
    """, (job_id,))
    
    row = cursor.fetchone()
    conn.close()
    
    return dict(row) if row else None
