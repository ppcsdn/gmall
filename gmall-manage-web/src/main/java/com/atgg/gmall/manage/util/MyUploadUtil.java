package com.atgg.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Pstart
 * @create 2019-10-14 12:15
 */
public class MyUploadUtil {
    //参数二进制数据对象
    public static String upload_image(MultipartFile multipartFile){
        //初始化客户端
        String trackerPath = MyUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();
        try {
            ClientGlobal.init(trackerPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        //先获得tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //再获得storage,默认服务为null
        StorageClient storageClient = new StorageClient(connection, null);
        //uri,扩展名,元数据描述
        //group1
        //M00/00/00/wKgnZF2j9hCAL-qXAAYvB_bL5fo187.jpg
        String urlstring = "http://192.168.39.100";
        try {
            byte [] bytes = multipartFile.getBytes();
            //获取图片扩展名
            String originalFilename = multipartFile.getOriginalFilename();

            int lastPoint = originalFilename.lastIndexOf(".");
            String extention = originalFilename.substring(lastPoint + 1);

            String[] uris = storageClient.upload_file(bytes, extention, null);
            for (String uri : uris) {
                System.out.println(uri);
                urlstring = urlstring + "/" + uri;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        return urlstring;
    }
}
