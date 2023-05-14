# from websocket import create_connection
#
# ws = create_connection("ws://localhost:8081/websocket/jetson")
# result = ws.recv()
# print(result)
import asyncio

import websockets
import requests
import os


async def process():
    async with websockets.connect("wss://gaoyuanwang.top/websocket/jetson") as ws:
        while True:
            try:
                message = await asyncio.wait_for(ws.recv(), timeout=30)
                print('Received: ' + message)
                if message[:7] == "client:":
                    print('Start to process: ' + message[7:])
                    reality = requests.get("https://gaoyuanwang.top/photoDownload?filename=" + message[7:])
                    open("/home/gaoyuan/GAN/test/val/reality.jpg", "wb").write(reality.content)
                    os.system("python /home/gaoyuan/GAN/gan-application/test3.py --dataroot /home/gaoyuan/GAN/test   --results_dir /home/gaoyuan/GAN/result  \
                    --restore_G_path /home/gaoyuan/GAN/latest_net_G.pth   --real_stat_path  /home/gaoyuan/GAN/photo2cartoon.npz \
                    --need_profile --config_str 16_24_24_24_56_56_32_40")
                else:
                    print('Start initiate the camera to find face')
                    os.system("python /home/gaoyuan/GAN/findFace.py")
            except asyncio.TimeoutError as e:
                continue
            except websockets.exceptions.ConnectionClosed as e:
                print('连接已经关闭')
                break


loop = asyncio.get_event_loop()
loop.run_until_complete(process())
loop.close()
