package com.project.mapapp.service;

import com.project.mapapp.model.entity.Application;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【application】的数据库操作Service
* @createDate 2025-03-23 15:33:22
*/
public interface ApplicationService extends IService<Application> {

    Application submitApplication(String guardianId, String wardDeviceId);

    boolean confirmApplication(Long applicationId, Boolean isApproved);

    Long getApplicationId(String guardianId, String wardDeviceId);

}
