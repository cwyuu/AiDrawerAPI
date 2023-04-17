package com.example.aidrawerapi.pojo;

import lombok.Data;

@Data
public class Model {
    private String title;
    private String model_name;
    private String hash;
    private String sha256;
    private String filename;
    private String config;
}
