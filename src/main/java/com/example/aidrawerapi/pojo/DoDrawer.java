package com.example.aidrawerapi.pojo;

import lombok.Data;

@Data
public class DoDrawer {
    private Integer steps;
    private String sampler_name;
    private String prompt;
    private String negative_prompt;
    private Integer width;
    private Integer height;
    private Integer cfg_scale;
    private OverrideSettings override_settings;
}
