# Jetson 开发记录
## 环境配置
* 使用Jetpack4.2 默认为Python3.6.9
* 科学上网， 需要使用v2rayA和v2ray-core，安装报错Certificate verification failed: The certificate is NOT trusted.需要执行
```
sudo apt install curl
curl -Ls https://mirrors.v2raya.org/go.sh | sudo bash
sudo systemctl disable v2ray --now
wget -qO - https://apt.v2raya.org/key/public-key.asc | sudo tee /etc/apt/trusted.gpg.d/v2raya.asc
echo "deb https://apt.v2raya.org/ v2raya main" | sudo tee /etc/apt/sources.list.d/v2raya.list
sudo apt-get install --reinstall ca-certificates
sudo apt-get update
sudo apt install v2raya
sudo systemctl start v2raya.service
sudo systemctl enable v2raya.service
```
* 安装完毕v2rayA不依赖systemd运行，默认在localhost:2017运行，开启全局透明代理。setting处可以设置，官方说明文档推荐的第一种代理模式
* 设置 nvcc，设置Python 3.6.9为默认Python
```
sudo vim ~/.bashrc
#文件最后添加此三行
export PATH=/usr/local/cuda/bin:$PATH
export LD_LIBRARY_PATH=/usr/local/cuda/lib64:$LD_LIBRARY_PATH
export CUDA_HOME=/usr/local/cuda
source ~./bashrc
nvcc -V
sudo update-alternatives --install /usr/bin/python python /usr/bin/python2 100
sudo update-alternatives --install /usr/bin/python python /usr/bin/python3 150
```
* 安装pip
```
sudo apt install python3-pip
```
* 根据官网提示安装Pytorch=1.4.0和torchvision=0.5.0[在Jetsonnano上安装Pytorch和torchvision](https://forums.developer.nvidia.com/t/pytorch-for-jetson/72048)
```
wget https://nvidia.box.com/shared/static/p57jwntv436lfrd78inwl7iml6p13fzh.whl -O torch-1.4.0-cp36-cp36m-linux_aarch64.whl
sudo apt-get install python3-pip libopenblas-base libopenmpi-dev libomp-dev
pip3 install Cython
pip3 install numpy==1.18.1 torch-1.4.0-cp36-cp36m-linux_aarch64.whl
sudo apt-get install libjpeg-dev zlib1g-dev libpython3-dev libavcodec-dev libavformat-dev libswscale-dev
git clone --branch v0.5.0 https://github.com/pytorch/vision torchvision
cd torchvision
export BUILD_VERSION=0.5.0
python3 setup.py install --user
cd ../
```
* 官网步骤安装的pillow版本过高，需要执行
```
pip3 install 'pillow<7'
```
* 安装opencv4.2.0，下载源代码包，[教程](https://blog.csdn.net/qq_43448818/article/details/126712923)
```
sudo apt-get install cmake
unzip opencv-4.2.0.zip
cd opencv-4.2.0
mkdir build && cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local ..
sudo make -j4 
sudo make install
sudo vim /etc/ld.so.conf.d/opencv.conf
# 添加/usr/local/lib
sudo ldconfig
sudo vim ~/.bashrc
# 添加
# PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/local/lib/pkgconfig
# export PKG_CONFIG_PATH
source ~/.bashrc
```
* 安装scipy过程中会报错，需先执行以下命令
```
sudo apt-get install gfortran
sudo apt-get update
sudo apt-get install -y build-essential libatlas-base-dev
```
* 安装torchprofile=0.0.1，直接安装会报错，需要忽视依赖
```
pip3 install torchprofile=0.0.1 --no-dependencies
```
* 其它包可按照GitHub上提示从requirements.txt安装
```
mkdir GAN
cd GAN
git clone https://github.com/mit-han-lab/gan-compression.git
cd gan-compression
sudo vim requirements.txt
# 删去已安装的pytorch opencv torchvision torchprofile
pip3 install -r requirements.txt
```
---
## 测试命令
```
python test2.py --dataroot /home/gaoyuan/GAN/test \
  --results_dir /home/gaoyuan/GAN/result \
  --restore_G_path /home/gaoyuan/GAN/latest_net_G.pth \
  --real_stat_path  /home/gaoyuan/GAN/photo2cartoon.npz \
  --need_profile --config_str 16_24_24_24_56_56_32_40 --dataset_mode single
```
---
## 摄像头调用
* 开启摄像头`nvgstcapture`
```
ls /dev/video* 
sudo apt install v4l-utils 
v4l2-ctl --device=/dev/video0 --list-formats-ext #摄像头相关信息
pip3 install traitlets==4.3.3
git clone https://github.com/NVIDIA-AI-IOT/jetcam
cd jetcam
sudo python3 setup.py install
```
* `photo.py`包含调取摄像头并存储照片的相关代码
---
## 开发
* `gan-application`文件夹为应用
* `gan-compression-master`为GitHub源代码
* 各个test为测试代码
* `test1.py`去除FID的相关运算
* `test2.py`优化了图片获取的方式
* `test3.py`实现利用板载摄像头拍摄照片生成卡通图片
```
python gan-application/test3.py --dataroot /home/gaoyuan/GAN/test   --results_dir /home/gaoyuan/GAN/result  \
--restore_G_path /home/gaoyuan/GAN/latest_net_G.pth   --real_stat_path  /home/gaoyuan/GAN/photo2cartoon.npz \
--need_profile --config_str 16_24_24_24_56_56_32_40
```
* 删除options下无关的代码，将dataset_mode默认值改为single
---
## 客户端开发
* 使用websocket实现，整体流程为前端上传图片后，服务器接收并保存，继而通过websocket向jetson发送消息，jetson接收到消息后下载图片，经过处理后上传至服务器，服务器通过websocket向客户端发送消息，客户端接收到之后下载处理后的图片
* 前端使用vue + element UI
* 后端使用springboot
* jetson使用python实现websocket，具体代码位于InternetTest.py与test3.py