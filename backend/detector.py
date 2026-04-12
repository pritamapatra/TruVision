from typing import List, Dict
from pathlib import Path
import cv2
import numpy as np
import onnxruntime as ort

POLYMER_TYPES = ["PE", "PET", "PS", "PVC", "Nylon", "ABS"]
MODEL_PATH = Path("models/best080426.onnx")
INPUT_SIZE = 640
CONF_THRESHOLD = 0.25
IOU_THRESHOLD = 0.45

_session = ort.InferenceSession(str(MODEL_PATH), providers=["CPUExecutionProvider"])
_input_name = _session.get_inputs()[0].name

def _letterbox(image, new_shape=640, color=(114, 114, 114)):
    h, w = image.shape[:2]
    scale = min(new_shape / h, new_shape / w)
    nw, nh = int(round(w * scale)), int(round(h * scale))
    resized = cv2.resize(image, (nw, nh), interpolation=cv2.INTER_LINEAR)

    canvas = np.full((new_shape, new_shape, 3), color, dtype=np.uint8)
    dw = (new_shape - nw) / 2
    dh = (new_shape - nh) / 2
    left = int(round(dw - 0.1))
    right = int(round(dw + 0.1))
    top = int(round(dh - 0.1))
    bottom = int(round(dh + 0.1))
    canvas[top:new_shape-bottom, left:new_shape-right] = resized
    return canvas, scale, left, top

def _preprocess(image_path: str):
    image = cv2.imread(image_path)
    if image is None:
        raise ValueError(f"Could not read image: {image_path}")

    original_h, original_w = image.shape[:2]
    rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    padded, scale, pad_left, pad_top = _letterbox(rgb, INPUT_SIZE)
    tensor = padded.astype(np.float32) / 255.0
    tensor = np.transpose(tensor, (2, 0, 1))
    tensor = np.expand_dims(tensor, axis=0)
    return image, tensor, scale, pad_left, pad_top, original_w, original_h

def _xywh_to_xyxy(boxes):
    xyxy = np.zeros_like(boxes)
    xyxy[:, 0] = boxes[:, 0] - boxes[:, 2] / 2
    xyxy[:, 1] = boxes[:, 1] - boxes[:, 3] / 2
    xyxy[:, 2] = boxes[:, 0] + boxes[:, 2] / 2
    xyxy[:, 3] = boxes[:, 1] + boxes[:, 3] / 2
    return xyxy

def _clip(value, low, high):
    return max(low, min(int(round(value)), high))

def detect_microplastics(image_path: str) -> List[Dict]:
    _, tensor, scale, pad_left, pad_top, original_w, original_h = _preprocess(image_path)

    outputs = _session.run(None, {_input_name: tensor})
    preds = outputs[0]

    if preds.ndim != 3 or preds.shape[0] != 1:
        raise ValueError(f"Unexpected output shape: {preds.shape}")

    preds = preds[0].transpose(1, 0)

    if preds.shape[1] != 4 + len(POLYMER_TYPES):
        raise ValueError(
            f"Unexpected channel count: {preds.shape[1]}, expected {4 + len(POLYMER_TYPES)}"
        )

    boxes_xywh = preds[:, :4]
    class_scores = preds[:, 4:]

    class_ids = np.argmax(class_scores, axis=1)
    confidences = class_scores[np.arange(class_scores.shape[0]), class_ids]

    keep = confidences >= CONF_THRESHOLD
    boxes_xywh = boxes_xywh[keep]
    confidences = confidences[keep]
    class_ids = class_ids[keep]

    if len(boxes_xywh) == 0:
        print(f"Processing image: {image_path} -> 0 detections")
        return []

    boxes_xyxy = _xywh_to_xyxy(boxes_xywh)

    boxes_xyxy[:, [0, 2]] -= pad_left
    boxes_xyxy[:, [1, 3]] -= pad_top
    boxes_xyxy /= scale

    boxes_for_nms = []
    for box in boxes_xyxy:
        x1, y1, x2, y2 = box
        boxes_for_nms.append([
            float(x1),
            float(y1),
            float(max(0.0, x2 - x1)),
            float(max(0.0, y2 - y1)),
        ])

    indices = cv2.dnn.NMSBoxes(
        boxes_for_nms,
        confidences.tolist(),
        CONF_THRESHOLD,
        IOU_THRESHOLD
    )

    if indices is None or len(indices) == 0:
        print(f"Processing image: {image_path} -> 0 detections after NMS")
        return []

    indices = np.array(indices).reshape(-1)

    detections = []
    det_id = 1
    for idx in indices:
        x1, y1, x2, y2 = boxes_xyxy[idx]

        x1 = _clip(x1, 0, original_w - 1)
        y1 = _clip(y1, 0, original_h - 1)
        x2 = _clip(x2, 0, original_w - 1)
        y2 = _clip(y2, 0, original_h - 1)

        if x2 <= x1 or y2 <= y1:
            continue

        class_id = int(class_ids[idx])
        confidence = float(confidences[idx])

        detections.append({
            "id": det_id,
            "polymer_type": POLYMER_TYPES[class_id],
            "confidence": round(confidence, 4),
            "bbox": [x1, y1, x2, y2]
        })
        det_id += 1

    detections.sort(key=lambda d: d["confidence"], reverse=True)
    print(f"Processing image: {image_path} -> {len(detections)} detections")
    return detections
