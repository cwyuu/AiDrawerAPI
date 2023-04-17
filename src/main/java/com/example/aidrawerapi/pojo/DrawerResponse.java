package com.example.aidrawerapi.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DrawerResponse {
    private List<String> images;
    private Object parameters;
    private String info;
}
