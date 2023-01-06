<template>
    <el-upload
    class="upload-demo"
    ref="upload"
    action="http://localhost:8081/photoUpload"
    :on-preview="handlePreview"
    :on-remove="handleRemove"
    :file-list="fileList"
    :auto-upload="false">
    <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
    <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
    <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
    </el-upload>
</template>

<script>
export default {
    data() {
      return {
        fileList: [],
        filename:''
      };
    },
    mounted(){
      //this.connectWebsocket()
    },
    methods: {
      submitUpload() {
        this.$refs.upload.submit();
        this.connectWebsocket()
      },
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      connectWebsocket() {
        let that = this
        let websocket;
        if (typeof WebSocket === "undefined") {
          console.log("您的浏览器不支持WebSocket");
          return;
        } else {
          let protocol = "ws";
          let url = "";
          if (window.location.protocol == "https:") {
            protocol = "wss";
          }
          // `${protocol}://window.location.host/echo`;
          url = `${protocol}://localhost:8081/websocket/user`;

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
      }
    }
}
</script>

<style>

</style>