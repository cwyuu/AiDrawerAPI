package com.example.aidrawerapi.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

public class MD5Util {
    public static String getMD5(String input) {
        try {
            // 获取MD5加密算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算MD5哈希值
            byte[] messageDigest = md.digest(input.getBytes());
            // 将哈希值转换为16进制字符串
            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(16);
            // 如果字符串长度不足32位，则在左侧补0
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileName(){
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String originalFilename =timestamp;
        try {
            return sha256(originalFilename);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "error";
        }
    }

    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static String md5(byte[] data) {
        try {
            // 创建一个 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 使用 byte[] 更新 MessageDigest
            md.update(data);

            // 计算 MD5 哈希值
            byte[] md5Bytes = md.digest();

            // 将 MD5 哈希值转换为 Base64 编码
            return Base64.getEncoder().encodeToString(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }
}

