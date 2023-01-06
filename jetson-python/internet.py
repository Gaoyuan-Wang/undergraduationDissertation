import requests
from urllib3 import encode_multipart_formdata

file_data = {'file': ('reality.jpg', open('/jetson-python/reality.jpg', 'rb').read())}
encode_data = encode_multipart_formdata(file_data)
data = encode_data[0]
header = {'Content-Type': encode_data[1]}
requests.post('http://192.168.3.99:8081/cartoonUpload', headers=header, data=data)
