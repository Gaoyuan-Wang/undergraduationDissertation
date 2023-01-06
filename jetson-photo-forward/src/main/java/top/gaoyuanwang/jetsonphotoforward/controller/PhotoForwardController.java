package top.gaoyuanwang.jetsonphotoforward.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.gaoyuanwang.jetsonphotoforward.service.MqttGateway;
import top.gaoyuanwang.jetsonphotoforward.utils.WebSocket;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@Slf4j
public class PhotoForwardController {

    @Resource
    private MqttGateway mqttGateWay;
    @Resource
    private WebSocket webSocket;

    @RequestMapping("/photoUpload")
    public String photoUpload(@RequestParam("file") MultipartFile photo, HttpSession session) throws IOException {
        String filename = photo.getOriginalFilename();
        ServletContext servletContext = session.getServletContext();
        String path = servletContext.getRealPath(File.separator + "photo");
        File file = new File(path);
        if(!file.exists()) file.mkdir();
        String finalPath = path + File.separator + filename;
        log.info(finalPath);
        photo.transferTo(new File(finalPath));
        mqttGateWay.sendToMqtt("realityInform",filename);
        webSocket.sendOneMessage("jetson",filename);
        return "success";
    }

    @RequestMapping("/photoDownload")
    public ResponseEntity<byte[]> photoDownload(String filename, HttpSession session) throws IOException {
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath("/photo"+ File.separator + filename);
        log.info(realPath);
        FileInputStream is = new FileInputStream(realPath);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.add("Content-Disposition","attachment;filename=" + filename);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        is.close();
        return responseEntity;
    }

    @RequestMapping("/cartoonUpload")
    public String cartoonUpload(@RequestParam("file")MultipartFile photo, HttpSession session) throws IOException {
        String filename = photo.getOriginalFilename();
        ServletContext servletContext = session.getServletContext();
        String path = servletContext.getRealPath(File.separator + "photo");
        File file = new File(path);
        if(!file.exists()) file.mkdir();
        String finalPath = path + File.separator + filename;
        photo.transferTo(new File(finalPath));
        mqttGateWay.sendToMqtt("cartoonInform",filename);
        webSocket.sendOneMessage("user",filename);
        return "success";
    }

    @RequestMapping("/cartoonDownload")
    public ResponseEntity<byte[]> cartoonDownload(String filename, HttpSession session) throws IOException {
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath("/photo"+ File.separator + filename);
        FileInputStream is = new FileInputStream(realPath);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.add("Content-Disposition","attachment;filename=" + filename);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        is.close();
        return responseEntity;
    }
}
