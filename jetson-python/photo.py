from jetcam.csi_camera import CSICamera
import cv2

#CSI-0
camera0 = CSICamera(capture_device=0, width=224, height=224)
image0 = camera0.read()

cv2.imwrite("test/val/1.jpg", image0)  # 存图像