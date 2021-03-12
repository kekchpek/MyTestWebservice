package com.kekchpek.mytestwebservice;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        GetPostService service = GetPostService.createNew();
        try {
            service.startup(12345);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
