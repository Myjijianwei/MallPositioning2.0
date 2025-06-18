package com.project.mapapp.service;

import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.LocationData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author jjw
* @description 针对表【location_data_test(位置数据表)】的数据库操作Service
* @createDate 2025-03-25 09:36:34
*/
public interface LocationDataService extends IService<LocationData> {
    boolean processLocation(
            String deviceId,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            Long guardianId
            );

    LocationResponseDTO getLatestLocation(String deviceId, Long guardianId);

    List<LocationData> queryHistory(String deviceId, LocalDateTime start, LocalDateTime end);
}
