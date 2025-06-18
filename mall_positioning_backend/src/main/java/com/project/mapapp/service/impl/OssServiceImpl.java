package com.project.mapapp.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.project.mapapp.service.OssService;
import com.project.mapapp.utils.ConstantPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Service
public class OssServiceImpl implements OssService {

    private static final Logger logger = LoggerFactory.getLogger(OssServiceImpl.class);
    // 允许的文件类型
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Override
    public String uploadFileAvatar(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            logger.error("上传的文件为空，无法进行上传操作。");
            return null;
        }

        // 检查文件类型
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            logger.error("不支持的文件类型: {}", file.getContentType());
            return null;
        }

        // 工具类获取值
        String endpoint = ConstantPropertiesUtils.getEND_POINT();
        String accessKeyId = ConstantPropertiesUtils.getACCESS_KEY_ID();
        String accessKeySecret = ConstantPropertiesUtils.getACCESS_KEY_SECRET();
        String bucketName = ConstantPropertiesUtils.getBUCKET_NAME();

        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            // 创建 OSS 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 获取上传文件输入流
            inputStream = file.getInputStream();
            // 获取文件名称
            String fileName = file.getOriginalFilename();

            // 生成唯一文件名，这里使用雪花算法，避免UUID可能存在的字符问题
            String uniqueId = IdWorker.getIdStr();
            if (StrUtil.isNotEmpty(fileName)) {
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex != -1) {
                    String fileSuffix = fileName.substring(dotIndex);
                    fileName = uniqueId + fileSuffix;
                } else {
                    fileName = uniqueId + fileName;
                }
            } else {
                fileName = uniqueId;
            }

            // 把文件按照日期进行分类
            String datePath = new DateTime().toString("yyyy/MM/dd");
            fileName = datePath + "/" + fileName;

            // 调用 oss 方法实现上传
            ossClient.putObject(bucketName, fileName, inputStream);
            logger.info("文件上传成功，文件路径: {}", fileName);

            // 手动拼接上传到阿里云 oss 的路径
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (IOException e) {
            logger.error("读取文件输入流时发生错误", e);
        } catch (Exception e) {
            logger.error("上传文件到 OSS 时发生错误", e);
        } finally {
            // 关闭输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭文件输入流时发生错误", e);
                }
            }
            // 关闭 OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}