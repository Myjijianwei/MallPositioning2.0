package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.mapper.AlertMapper;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.enums.AlertStatus;
import com.project.mapapp.model.enums.AlertType;
import com.project.mapapp.service.AlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    private final AlertMapper alertMapper;

    public AlertServiceImpl(AlertMapper alertMapper) {
        this.alertMapper = alertMapper;
    }

    @Override
    public boolean hasPendingAlert(Long fenceId, String deviceId) {
        QueryWrapper<Alert> query = new QueryWrapper<>();
        query.eq("device_id", deviceId)
                .eq("fence_id", fenceId)
                .eq("status", "PENDING")
                .last("LIMIT 1");
        return this.getOne(query) != null;
    }

    @Override
    public boolean resolveAlert(Long alertId) {
        Alert alert = new Alert();
        alert.setId(alertId);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return this.updateById(alert);
    }

    @Override
    public List<Alert> getDeviceAlerts(String deviceId, AlertType type, boolean onlyPending) {
        QueryWrapper<Alert> query = new QueryWrapper<>();
        query.eq("device_id", deviceId);

        if (type != null) {
            query.eq("type", type.name());
        }

        if (onlyPending) {
            query.eq("status", "PENDING");
        }

        query.orderByDesc("triggered_at");
        return this.list(query);
    }

    @Override
    public boolean createAlert(Alert alert) {
        return this.save(alert);
    }
}