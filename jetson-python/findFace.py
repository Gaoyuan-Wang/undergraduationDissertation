import time

from PIL import Image
import face_recognition
from jetcam.csi_camera import CSICamera
import cv2

import os

# CSI-0
camera0 = CSICamera(capture_device=0, width=640, height=720)
image0 = camera0.read()
cv2.imwrite("/home/gaoyuan/GAN/test/val/reality.jpg", image0)  # 存图像


# Load the jpg file into a numpy array
image = face_recognition.load_image_file("/home/gaoyuan/GAN/test/val/reality.jpg")

# Find all the faces in the image using the default HOG-based model.
# This method is fairly accurate, but not as accurate as the CNN model and not GPU accelerated.
# See also: find_faces_in_picture_cnn.py

face_locations = face_recognition.face_locations(image)
while len(face_locations) == 0:
    image0 = camera0.read()
    cv2.imwrite("/home/gaoyuan/GAN/test/val/reality.jpg", image0)  # 存图像
    image = face_recognition.load_image_file("/home/gaoyuan/GAN/test/val/reality.jpg")
    face_locations = face_recognition.face_locations(image)
    print("No face found. Please try again.")
    time.sleep(0.5)

print("I found {} face(s) in this photograph.".format(len(face_locations)))

for face_location in face_locations:

    # Print the location of each face in this image
    top, right, bottom, left = face_location
    print("A face is located at pixel location Top: {}, Left: {}, Bottom: {}, Right: {}".format(top, left, bottom, right))
    # You can access the actual face itself like this:
    face_image = image[0:512, left:right]
    pil_image = Image.fromarray(face_image)
    pil_image.save("/home/gaoyuan/GAN/test/reality.jpg")
    break

if len(face_locations) >= 1:
    os.system("python /home/gaoyuan/GAN/gan-application/test3.py --dataroot /home/gaoyuan/GAN/test   --results_dir /home/gaoyuan/GAN/result  \
--restore_G_path /home/gaoyuan/GAN/latest_net_G.pth   --real_stat_path  /home/gaoyuan/GAN/photo2cartoon.npz \
--need_profile --config_str 16_24_24_24_56_56_32_40")