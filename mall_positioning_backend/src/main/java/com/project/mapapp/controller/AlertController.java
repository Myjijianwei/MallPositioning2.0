package com.project.mapapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.mapper.GeoFenceMapper;
import com.project.mapapp.model.dto.alert.AlertBatchUpdateDTO;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.GeoFence;
import com.project.mapapp.model.enums.AlertLevel;
import com.project.mapapp.model.enums.AlertStatus;
import com.project.mapapp.model.enums.AlertType;
import com.project.mapapp.model.vo.AlertDetailVO;
import com.project.mapapp.model.vo.AlertVO;
import com.project.mapapp.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/alerts")
@Slf4j
public class AlertController {

    @Autowired
    private AlertService alertService;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private GeoFenceMapper geoFenceMapper;

    @GetMapping
    public BaseResponse<Page<AlertVO>> listAlerts(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) AlertType type,
            @RequestParam(required = false) AlertLevel level,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        QueryWrapper<Alert> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(deviceId)) {
            queryWrapper.eq("device_id", deviceId);
        }
        if (type != null) {
            queryWrapper.eq("type", type.name());
        }
        if (level != null) {
            queryWrapper.eq("level", level.name());
        }
        if (status != null) {
            queryWrapper.eq("status", status.name());
        }
        if (StringUtils.isNotBlank(startTime)) {
            queryWrapper.ge("triggered_at", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le("triggered_at", endTime);
        }
        queryWrapper.orderByDesc("triggered_at");

        Page<Alert> page = alertService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success((Page<AlertVO>) page.convert(this::convertToVO));
    }

    @GetMapping("/{id}")
    public BaseResponse<AlertDetailVO> getAlertDetail(@PathVariable Long id) {
        Alert alert = alertService.getById(id);
        if (alert == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "警报不存在");
        }
        return ResultUtils.success(convertToDetailVO(alert));
    }

    @PostMapping("/updateStatus")
    public BaseResponse<Boolean> updateAlertStatus(@RequestBody  AlertBatchUpdateDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要更新的警报");
        }
        if (dto.getStatus() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态不能为空");
        }

        boolean result = alertService.update()
                .in("id", dto.getIds())
                .set("status", dto.getStatus().name())
                .set("resolved_at", LocalDateTime.now())
                .update();

        return ResultUtils.success(result);
    }

    @PostMapping("/batchUpdateStatus")
    public BaseResponse<Boolean> batchUpdateAlertStatus(@RequestBody  AlertBatchUpdateDTO dto) {
        return updateAlertStatus(dto);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAlert(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要删除的警报");
        }
        boolean result = alertService.removeByIds(ids);
        return ResultUtils.success(result);
    }

    @PutMapping("/{id}/resolve")
    public BaseResponse<Boolean> resolveAlert(@PathVariable Long id) {
        return updateSingleAlertStatus(id, AlertStatus.RESOLVED);
    }

    @PutMapping("/{id}/ignore")
    public BaseResponse<Boolean> ignoreAlert(@PathVariable Long id) {
        return updateSingleAlertStatus(id, AlertStatus.IGNORED);
    }

    private BaseResponse<Boolean> updateSingleAlertStatus(Long id, AlertStatus status) {
        Alert alert = alertService.getById(id);
        if (alert == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "警报不存在");
        }
        if (alert.getStatus() == status) {
            return ResultUtils.success(true);
        }

        alert.setStatus(AlertStatus.valueOf(status.name()));
        alert.setResolvedAt(LocalDateTime.now());
        return ResultUtils.success(alertService.updateById(alert));
    }

    private AlertVO convertToVO(Alert alert) {
        AlertVO vo = new AlertVO();
        BeanUtils.copyProperties(alert, vo);
        vo.setTriggeredAt(alert.getTriggeredAt());

        Device device = deviceMapper.selectById(alert.getDevice_id());
        if (device != null) {
            vo.setDeviceName(device.getName());
        }

        return vo;
    }

    private AlertDetailVO convertToDetailVO(Alert alert) {
        AlertDetailVO vo = new AlertDetailVO();
        BeanUtils.copyProperties(alert, vo);
        vo.setTriggeredAt(alert.getTriggeredAt());

        Device device = deviceMapper.selectById(alert.getDevice_id());
        if (device != null) {
            vo.setDeviceName(device.getName());
        }

        GeoFence fence = geoFenceMapper.selectById(alert.getFence_id());
        if (fence != null) {
            vo.setFenceName(fence.getName());
            vo.setCoordinates((String) fence.getCoordinates());
        }

        return vo;
    }
}