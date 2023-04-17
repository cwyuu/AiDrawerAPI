package com.example.aidrawerapi.pojo;

import lombok.Data;

@Data
public class DoDrawerDTO {
    private String sampler_name;
    private String prompt;
    private String negative_prompt;
    private Integer width;
    private Integer height;
    private OverrideSettings override_settings;
}
