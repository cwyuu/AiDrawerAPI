package com.example.aidrawerapi.Controller;

import com.example.aidrawerapi.pojo.*;
import com.example.aidrawerapi.util.AiDrawer;
import com.example.aidrawerapi.util.BeanUtils;
import com.example.aidrawerapi.util.JSONResult;
import com.example.aidrawerapi.util.MD5Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/aiDrawer")
public class AiDrawerController {
    @PostMapping("/getModels")
    public JSONResult getModels() {
        AiDrawer aiDrawer = new AiDrawer();
        try {
            CompletableFuture<List<Model>> aiDrawerControllerCompletableFuture=aiDrawer.getModelList();
            List<Model> modelList= aiDrawerControllerCompletableFuture.join();
            return new JSONResult(200,"获取成功",modelList);
        }catch (Exception e){
            return new JSONResult(500,"获取失败",null);
        }
    }

    @PostMapping("/drawer")
    public JSONResult doDrawer(@RequestBody DoDrawerDTO doDrawerDTO) throws JsonProcessingException, NoSuchAlgorithmException, UnsupportedEncodingException {
        DoDrawer doDrawer = BeanUtils.convert(doDrawerDTO, DoDrawer.class);
        doDrawer.setCfg_scale(7);
        doDrawer.setSteps(20);
        OverrideSettings override_settings = new OverrideSettings();
        override_settings.setSd_vae("kl-f8-anime2.ckpt");
        override_settings.setSd_model_checkpoint(doDrawerDTO.getOverride_settings().getSd_model_checkpoint());
        doDrawer.setOverride_settings(override_settings);
        AiDrawer aiDrawer = new AiDrawer();
        try {
            CompletableFuture<DrawerResponse> aiDrawerControllerCompletableFuture = aiDrawer.doDrawer(doDrawer);
            DrawerResponse drawerResponse = aiDrawerControllerCompletableFuture.join();
            String fileName= MD5Util.getFileName();
            CompletableFuture<String> saveImageCompletableFuture = aiDrawer.saveImage(drawerResponse,fileName);
            String result = saveImageCompletableFuture.join();
            if(Objects.equals(result, "1")){
                return new JSONResult(200,"生成成功",fileName);
            }else {
                return new JSONResult(400,"生成失败",null);
            }
        }catch (Exception e){
            return new JSONResult(500,"生成失败",null);
        }

    }
}
