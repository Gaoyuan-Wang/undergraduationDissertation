import os
# readInfo函数，根据文件夹路径读取文件夹下所有文件名
def readInfo():
    filePath = 'C:\\Users\\The winter of ink\\Desktop\\毕业设计\\selfie2anime\\trainB'
    name = os.listdir(filePath)         # os.listdir方法返回一个列表对象
    return name

# 程序入口
if __name__ == "__main__":
    fileList = readInfo()       # 读取文件夹下所有的文件名，返回一个列表
    print(fileList)
    file = open('train1B.meta', 'w')   # 创建文件，权限为写入
    for i in fileList:
        rowInfo = i + '\n'
        # print(rowInfo)
        file.write(rowInfo)