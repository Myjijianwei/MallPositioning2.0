package com.project.mapapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.enums.AlertType;

import java.util.List;

/**
* @author jjw
* @description 针对表【alert(报警记录表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface AlertService extends IService<Alert> {

    /**
     * 检查是否存在未解决的围栏警报
     */
    boolean hasPendingAlert(Long fenceId, String deviceId);

    /**
     * 标记警报为已解决
     */
    boolean resolveAlert(Long alertId);

    /**
     * 获取设备警报列表
     */
    List<Alert> getDeviceAlerts(String deviceId, AlertType type, boolean onlyPending);

    /**
     * 创建新警报
     */
    boolean createAlert(Alert alert);
}
