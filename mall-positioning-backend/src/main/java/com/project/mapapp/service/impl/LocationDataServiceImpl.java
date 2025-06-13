package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.mapper.GeoFenceMapper;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.LocationData;
import com.project.mapapp.service.GeoFenceService;
import com.project.mapapp.service.LocationDataService;
import com.project.mapapp.mapper.LocationDataMapper;
import com.project.mapapp.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author jjw
* @description 针对表【location_data_test(位置数据表)】的数据库操作Service实现
* @createDate 2025-03-25 09:36:34
*/
@Service
public class LocationDataServiceImpl extends ServiceImpl<LocationDataMapper, LocationData>
    implements LocationDataService {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private LocationDataMapper locationDataTestMapper;

    @Autowired
    private GeoFenceService geoFenceService;

    /**
     * 处理位置上报
     */
    @Override
    @Transactional
    public boolean processLocation(
            String deviceId,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            Long guardianId) {

        // 1. 保存到数据库
        LocationData location = new LocationData();
        location.setDevice_id(deviceId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setGuardian_id(guardianId);

        int insert = locationDataTestMapper.insert(location);

        // 2. 通过WebSocket通知监护人
        LocationResponseDTO dto = convertToResponseDTO(location);
        webSocketService.notifyGuardian(guardianId, dto);
        geoFenceService.checkLocation(dto);
        return insert > 0;
    }

    /**
     * 获取最新位置
     */
    public LocationResponseDTO getLatestLocation(String deviceId, Long guardianId) {
        QueryWrapper<LocationData> query = new QueryWrapper<>();
        query.eq("device_id", deviceId)
                .eq("guardian_id", guardianId)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        LocationData location = locationDataTestMapper.selectOne(query);
        return convertToResponseDTO(location);
    }

    @Override
    public List<LocationData> queryHistory(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<LocationData> queryWrapper = new QueryWrapper<>();

        // 设备ID条件
        queryWrapper.eq("device_id", deviceId);

        // 时间范围条件
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }

        // 按时间升序排序
        queryWrapper.orderByAsc("create_time");

        return this.list(queryWrapper);
    }

    private LocationResponseDTO convertToResponseDTO(LocationData location) {
        if (location == null) return null;

        LocationResponseDTO dto = new LocationResponseDTO();
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());
        dto.setDeviceId(location.getDevice_id());
        dto.setCreateTime(String.valueOf(location.getCreate_time()));
        return dto;
    }

}




