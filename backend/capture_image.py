import subprocess
import os
from datetime import datetime

def capture_from_camera(output_path: str, resolution: str = "640x480") -> dict:
    """
    Capture image from USB microscope using fswebcam.
    
    Args:
        output_path: Full path where image should be saved
        resolution: Camera resolution (default 640x480 for USB microscope)
    
    Returns:
        dict with success status, path, timestamp, and any error
    """
    try:
        timestamp = datetime.now().isoformat()
        
        # Ensure output directory exists (only if path contains directory)
        output_dir = os.path.dirname(output_path)
        if output_dir:
            os.makedirs(output_dir, exist_ok=True)
        
        # Build fswebcam command
        cmd = [
            "fswebcam",
            "-r", resolution,
            "--no-banner",
            "-D", "1",
            output_path
        ]
        
        print(f"[Camera] Capturing image to {output_path}...")
        
        # Run capture command
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=10
        )
        
        if result.returncode == 0 and os.path.exists(output_path):
            file_size = os.path.getsize(output_path)
            print(f"[Camera] Capture successful: {file_size} bytes")
            return {
                "success": True,
                "path": output_path,
                "timestamp": timestamp,
                "file_size": file_size,
                "resolution": resolution,
                "method": "camera"
            }
        else:
            error_msg = result.stderr if result.stderr else "Unknown capture error"
            print(f"[Camera] Capture failed: {error_msg}")
            return {
                "success": False,
                "error": error_msg,
                "timestamp": timestamp,
                "method": "camera"
            }
            
    except subprocess.TimeoutExpired:
        return {
            "success": False,
            "error": "Camera capture timeout (>10s)",
            "method": "camera"
        }
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "method": "camera"
        }

if __name__ == "__main__":
    # Test the capture function
    test_path = "test_module_capture.jpg"
    result = capture_from_camera(test_path)
    print(f"\nCapture result: {result}")
