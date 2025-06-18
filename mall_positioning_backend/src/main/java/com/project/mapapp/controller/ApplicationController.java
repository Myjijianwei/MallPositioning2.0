package com.project.mapapp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apply")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationMapper applicationMapper;

    @PostMapping("/submit")
    public BaseResponse<Application> submitApplication(
            @RequestParam String guardianId,
            @RequestParam String wardDeviceId) {
        Application application = applicationService.submitApplication(guardianId, wardDeviceId);
        return ResultUtils.success(application);
    }

    @PostMapping("/getApplicationsByGId")
    public BaseResponse<List<Application>> getApplicationsByGid(String guardianId) {
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("guardian_id", guardianId);
        List<Application> applications = applicationMapper.selectList(queryWrapper);
        return ResultUtils.success(applications);
    }

    @PostMapping("/confirm")
    public BaseResponse<Boolean> confirmApplication(@RequestParam Long notificationId, @RequestParam Boolean isApproved) {
        try {
            boolean result = applicationService.confirmApplication(notificationId, isApproved);
            return ResultUtils.success(result);
        } catch (BusinessException e) {
            return ResultUtils.error(e.getCode(), e.getMessage());
        }
    }

}
