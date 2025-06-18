package com.project.mapapp.controller;

import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.service.OssService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private OssService ossService;

    // 定义允许的最大文件大小（10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping
    @ApiOperation(value = "上传文件")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.error("上传的文件为空，无法进行上传操作。");
            return ResultUtils.error(HttpStatus.BAD_REQUEST.value(), "上传的文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.error("上传的文件大小超过限制，文件大小: {}", file.getSize());
            return ResultUtils.error(HttpStatus.BAD_REQUEST.value(), "上传的文件大小不能超过 10MB");
        }

        String url = ossService.uploadFileAvatar(file);
        if (url == null) {
            logger.error("文件上传失败");
            return ResultUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件上传失败，请稍后重试");
        }
        logger.info("文件上传成功，文件 URL: {}", url);
        return ResultUtils.success(url);
    }
}
