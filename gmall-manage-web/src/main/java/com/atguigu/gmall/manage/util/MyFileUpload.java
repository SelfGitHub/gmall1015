package com.atguigu.gmall.manage.util;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyFileUpload {

    public static  String uploadImage(MultipartFile multipartFile) {
        String url = "http://192.168.76.105";

        try {
            //加载配置文件
            String path = MyFileUpload.class.getClassLoader().getResource("tracker.conf").getPath();
            //将配置信息加入到fdfs中
            ClientGlobal.init(path);
            //创建Tracker
            TrackerClient trackerClient = new TrackerClient();

            //获取连接
            TrackerServer connection = trackerClient.getConnection();
            //获得上传的storage
            StorageClient storageClient = new StorageClient(connection, null);
            //截取 文件名
            String s = StringUtils.substringAfterLast(multipartFile.getOriginalFilename(), ".");

            String[] jpgs = storageClient.upload_file(multipartFile.getBytes(), s, null);


            for (String jpg : jpgs) {
               url = url +"/" + jpg;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        return  url;
    }
}