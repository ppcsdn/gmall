package com.atgg.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {
    @Test
    public void contextLoads() throws IOException, MyException {
        //初始化客户端
        String trackerPath = GmallManageWebApplicationTests.class.getClassLoader().getResource("tracker.conf").getPath();
        ClientGlobal.init(trackerPath);

        //先获得tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        //再获得storage,默认服务为null
        StorageClient storageClient = new StorageClient(connection, null);
        //uri,扩展名,元数据描述
        //group1
        //M00/00/00/wKgnZF2j9hCAL-qXAAYvB_bL5fo187.jpg
        String[] uris = storageClient.upload_file("C:\\Users\\23008\\Pictures\\Saved Pictures\\bizhi.jpg", "jpg", null);
        String urlstring = "http://192.168.39.100";
        for (String uri : uris) {
            System.out.println(uri);
            urlstring = urlstring + "/" + uri;
        }
        System.out.println(urlstring);
    }

}
