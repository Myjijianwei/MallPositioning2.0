package com.project.mapapp.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ConstantPropertiesUtils {

    private static String END_POINT;
    private static String ACCESS_KEY_ID;
    private static String ACCESS_KEY_SECRET;
    private static String BUCKET_NAME;

    @Value("${aliyun.oss.endpoint}")
    private String endPoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @PostConstruct
    public void init() {
        END_POINT = endPoint;
        ACCESS_KEY_ID = accessKeyId;
        ACCESS_KEY_SECRET = accessKeySecret;
        BUCKET_NAME = bucketName;
    }

    public static String getEND_POINT() {
        return END_POINT;
    }

    public static String getACCESS_KEY_ID() {
        return ACCESS_KEY_ID;
    }

    public static String getACCESS_KEY_SECRET() {
        return ACCESS_KEY_SECRET;
    }

    public static String getBUCKET_NAME() {
        return BUCKET_NAME;
    }
}