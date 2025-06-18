package com.project.mapapp.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.model.dto.location.LocationReportDTO;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.LocationData;
import com.project.mapapp.service.LocationDataService;
import com.project.mapapp.service.UserService;
import com.project.mapapp.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Validated
@Slf4j
public class LocationDataController {

    private final LocationDataService locationService;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final DeviceMapper deviceMapper;
    private final ApplicationMapper applicationMapper;
    private final LocationDataService locationDataTestService;

    /**
     * 上报当前位置
     */
    @PostMapping("/report")
    public BaseResponse<String> reportLocation(@RequestBody LocationReportDTO dto) {
        log.info("收到位置上报: {}", dto);
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",dto.getWardId());
        String deviceId = deviceMapper.selectOne(queryWrapper).getId();
        dto.setDeviceId(deviceId);

        QueryWrapper<Application> applicationQueryWrapper = new QueryWrapper<>();
        applicationQueryWrapper.eq("ward_device_id",deviceId);
        Long guardianId = Long.valueOf(applicationMapper.selectOne(applicationQueryWrapper).getGuardian_id());
        dto.setGuardianId(guardianId);


        boolean success = locationService.processLocation(
                dto.getDeviceId(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getAccuracy(),
                dto.getGuardianId()
        );

        if (success) {
            // 主动触发一次推送测试
            LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
            BeanUtils.copyProperties(dto, locationResponseDTO);
            webSocketService.notifyGuardian(dto.getGuardianId(), locationResponseDTO);
            return ResultUtils.success("上报成功");
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR);
    }

    /**
     * 获取最新位置
     */
    @GetMapping("/latestLocationByDeviceID")
    public BaseResponse<LocationResponseDTO> getLatestLocation(String deviceId, HttpServletRequest request) {
        Long guardianId = userService.getLoginUser(request).getId();
        LocationResponseDTO response = locationService.getLatestLocation(deviceId, guardianId);
        return ResultUtils.success(response);
    }



    @GetMapping("/history")
    public BaseResponse<List<LocationResponseDTO>> getLocationHistory(
            @RequestParam String deviceId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {

        // 3. 转换时间参数
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (startTime != null) {
                start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (endTime != null) {
                end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (DateTimeParseException e) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "时间格式不正确");
        }

        // 4. 查询数据库
        List<LocationData> locations = locationDataTestService.queryHistory(deviceId, start, end);

        // 5. 转换为DTO
        List<LocationResponseDTO> dtos = locations.stream()
                .sorted(Comparator.comparing(LocationData::getCreate_time)) // 按时间排序
                .map(loc -> {
                    LocationResponseDTO dto = new LocationResponseDTO();
                    dto.setDeviceId(loc.getDevice_id());
                    dto.setLongitude(loc.getLongitude());
                    dto.setLatitude(loc.getLatitude());
                    dto.setAccuracy(loc.getAccuracy());
                    dto.setCreateTime(loc.getCreate_time().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResultUtils.success(dtos);
    }
}