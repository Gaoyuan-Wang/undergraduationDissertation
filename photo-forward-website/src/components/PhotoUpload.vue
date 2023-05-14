<template>
    <div>
      <el-container>
        <el-main>
          <el-upload
          class="upload-demo"
          ref="upload"
          action="https://gaoyuanwang.top/photoUpload"
          :before-upload="beforeAvatarUpload"
          :file-list="fileList"
          :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
          <div slot="tip" class="el-upload__tip">只能上传jpg/png文件</div>
          </el-upload>
          <br>
          <el-button type="primary" @click="camera" size="medium">使用Jetson板载摄像头捕捉人脸</el-button>
        </el-main>
        <el-footer>
          <el-link href="https://beian.miit.gov.cn" target="_blank">皖ICP备2021017566号</el-link>
        </el-footer>
      </el-container>
    </div>
</template>

<script>
export default {
    data() {
      return {
        fileList: [],
        filename:''
      };
    },
    created(){
      this.connectWebsocket()
    },
    methods: {
      // 上传文件之前
      beforeAvatarUpload(file) {
        let size10M = file.size / 1024 / 1024 < 50
        if (!size10M) {
          this.$message.warning('上传文件大小不能超过 50MB!');
          return false
        }
      },
      submitUpload() {
        this.$refs.upload.submit();
        //this.connectWebsocket()
      },
      connectWebsocket() {
        let that = this
        let websocket;
        if (typeof WebSocket === "undefined") {
          console.log("您的浏览器不支持WebSocket");
        } else {
          let url = "";
          // `${protocol}://window.location.host/echo`;
          url = `wss://gaoyuanwang.top/websocket/user`;

          // 打开一个websocket
          websocket = new WebSocket(url);
          // 建立连接
          websocket.onopen = () => {
            // 发送数据
            websocket.send("发送数据");
            console.log("websocket发送数据中");
          };
          // 客户端接收服务端返回的数据
          websocket.onmessage = evt => {
            that.filename = evt.data
            console.log("websocket返回的数据：", evt);
            this.axios.get("/cartoonDownload", {
                params: {
                  filename: that.filename
                },
                responseType: 'blob', // 切记类型 blob
            }).then((res) => {
                console.log(res);
                const { data } = res
                const reader = new FileReader()
                reader.readAsDataURL(data)
                reader.onload = (ev) => {
                  const img = new Image()
                  img.src = ev.target.result
                  document.body.appendChild(img)
              }
            }).catch((err) => {
                console.log(err);
            });
          };
          // 发生错误时
          websocket.onerror = evt => {
            console.log("websocket错误：", evt);
          };
          // 关闭连接
          websocket.onclose = evt => {
            console.log("websocket关闭：", evt);
          };
        }
      },
      camera(){
        this.axios.get("/cameraCatch", {
          responseType: 'blob', // 切记类型 blob
      }).then((res) => {
          console.log(res);
        }
      ).catch((err) => {
          console.log(err);
      });
      }
    }
}
</script>

<style>

</style>
