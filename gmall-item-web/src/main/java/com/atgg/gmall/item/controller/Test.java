package com.atgg.gmall.item.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {

        String json = "{\"|253|258|262\":\"112\",\"|254|257|263\":\"111\",\"|255|256|261\":\"113\"}";

        File file = new File("D:/spu_68.json");

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(json.getBytes());
    }
}
