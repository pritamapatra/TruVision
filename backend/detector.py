import random
from typing import List, Dict

POLYMER_TYPES = ["PE", "PET", "PS", "PVC", "Nylon", "ABS"]

def detect_microplastics(image_path: str) -> List[Dict]:
    num_detections = random.randint(3, 7)
    detections = []
    for i in range(num_detections):
        polymer = random.choice(POLYMER_TYPES)
        confidence = round(random.uniform(0.75, 0.95), 2)
        x1 = random.randint(50, 400)
        y1 = random.randint(50, 300)
        w = random.randint(30, 100)
        h = random.randint(30, 100)
        detections.append({
            "id": i + 1,
            "polymer_type": polymer,
            "confidence": confidence,
            "bbox": [x1, y1, x1 + w, y1 + h]
        })
    print(f"Processing image: {image_path}")
    return detections
