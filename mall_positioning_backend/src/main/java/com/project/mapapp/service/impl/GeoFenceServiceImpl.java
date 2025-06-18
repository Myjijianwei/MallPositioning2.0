package com.project.mapapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.mapper.LocationDataMapper;
import com.project.mapapp.model.dto.alert.AlertMessage;
import com.project.mapapp.model.dto.geofence.GeoFenceCreateRequest;
import com.project.mapapp.model.dto.geofence.GeoFenceUpdateRequest;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.entity.GeoFence;
import com.project.mapapp.mapper.GeoFenceMapper;
import com.project.mapapp.model.entity.LocationData;
import com.project.mapapp.model.enums.AlertType;
import com.project.mapapp.service.AlertService;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.service.GeoFenceService;
import com.project.mapapp.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoFenceServiceImpl extends ServiceImpl<GeoFenceMapper, GeoFence>
        implements GeoFenceService {

    private final GeoFenceMapper geoFenceMapper;
    private final AlertService alertService;
    private final WebSocketService webSocketService;
    private final DeviceService deviceService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final LocationDataMapper locationDataMapper;
    private final DeviceMapper deviceMapper;

    @Override
    public Boolean createGeoFence(GeoFenceCreateRequest request) {
        // 参数校验
        ThrowUtils.throwIf(Objects.isNull(request), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(CollectionUtils.isEmpty(request.getCoordinates()) ||
                request.getCoordinates().size() < 3, ErrorCode.PARAMS_ERROR, "至少需要3个坐标点");

        // 构建围栏实体
        GeoFence fence = new GeoFence();
        fence.setUser_id(request.getUserId());
        fence.setDevice_id(request.getDeviceId());
        fence.setName(request.getName());
        fence.setCoordinates(JSON.toJSONString(request.getCoordinates()));

        // 保存到数据库
        return this.save(fence);
    }

    @Override
    public List<GeoFence> listFences(String deviceId, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(deviceId == null || userId == null,
                ErrorCode.PARAMS_ERROR, "设备ID和用户ID不能为空");

        // 构建查询条件
        QueryWrapper<GeoFence> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId)
                .eq("user_id", userId)
                .orderByDesc("created_at");

        // 执行查询
        return geoFenceMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteGeoFence(Long fenceId, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(fenceId == null || userId == null,
                ErrorCode.PARAMS_ERROR, "围栏ID和用户ID不能为空");

        // 检查围栏是否存在及权限
        GeoFence fence = this.getById(fenceId);
        ThrowUtils.throwIf(fence == null, ErrorCode.NOT_FOUND_ERROR, "围栏不存在");

        // 执行删除
        return this.removeById(fenceId);
    }

    @Override
    public boolean updateGeoFence(GeoFenceUpdateRequest updateRequest, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null || userId == null,
                ErrorCode.PARAMS_ERROR, "参数不能为空");
        if (updateRequest.getCoordinates() != null) {
            ThrowUtils.throwIf(updateRequest.getCoordinates().size() < 3,
                    ErrorCode.PARAMS_ERROR, "至少需要3个坐标点");
        }

        // 查询现有围栏
        GeoFence existingFence = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(existingFence == null,
                ErrorCode.NOT_FOUND_ERROR, "围栏不存在");

        // 更新字段
        boolean needUpdate = false;
        if (updateRequest.getName() != null && !updateRequest.getName().equals(existingFence.getName())) {
            existingFence.setName(updateRequest.getName());
            needUpdate = true;
        }
        if (updateRequest.getCoordinates() != null) {
            String newCoordinates = JSON.toJSONString(updateRequest.getCoordinates());
            if (!newCoordinates.equals(existingFence.getCoordinates())) {
                existingFence.setCoordinates(newCoordinates);
                needUpdate = true;
            }
        }

        // 执行更新
        return !needUpdate || this.updateById(existingFence);
    }

    @Override
    public void checkLocation(LocationResponseDTO location) {
        List<GeoFence> fences = geoFenceMapper.selectByDeviceId(location.getDeviceId());
        if (CollectionUtils.isEmpty(fences)) return;

        try {
            Coordinate coord = new Coordinate(
                    location.getLongitude().doubleValue(),
                    location.getLatitude().doubleValue()
            );
            Point point = geometryFactory.createPoint(coord);

            for (GeoFence fence : fences) {
                Polygon polygon = parseCoordinates((String) fence.getCoordinates());
                if (!polygon.contains(point)) {
                    handleFenceBreach(fence, location);
                }
            }
        } catch (Exception e) {
            log.error("地理围栏校验失败", e);
        }
    }

    private Polygon parseCoordinates(String coordinatesJson) {
        try {
            List<List<Double>> coordinates = JSON.parseObject(
                    coordinatesJson,
                    new TypeReference<List<List<Double>>>() {}
            );

            Coordinate[] coords = coordinates.stream()
                    .map(p -> new Coordinate(p.get(0), p.get(1)))
                    .toArray(Coordinate[]::new);

            // 闭合多边形
            if (coords.length > 0 && !coords[0].equals(coords[coords.length-1])) {
                coords = Arrays.copyOf(coords, coords.length + 1);
                coords[coords.length - 1] = coords[0];
            }

            return geometryFactory.createPolygon(coords);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "围栏坐标格式错误");
        }
    }

    private void handleFenceBreach(GeoFence fence, LocationResponseDTO location) {
        try {
            // 1. 检查是否存在未解决的相同警报
            if (alertService.hasPendingAlert(fence.getId(), location.getDeviceId())) {
                return;
            }


            // 3. 创建新警报记录
            Alert alert = new Alert();
            alert.setDevice_id(location.getDeviceId());
            alert.setFence_id(fence.getId());
            alert.setType(AlertType.valueOf(AlertType.GEO_FENCE.name()));
            alert.setMessage(String.format("设备越出围栏%s", fence.getName()));
            alert.setLatitude(location.getLatitude());
            alert.setLongitude(location.getLongitude());


            // 4. 保存警报（在事务中）
            alertService.save(alert);

            // 5. 获取监护人ID
            Long guardianId = getGuardianId(location.getDeviceId());
            if (guardianId == null) {
                log.warn("未找到设备关联的监护人: {}", location.getDeviceId());
                return;
            }

            String deviceName = deviceMapper.selectById(location.getDeviceId()).getName();

            // 6. 发送WebSocket通知
            webSocketService.pushAlert(
                    guardianId,
                    new AlertMessage(
                            AlertType.GEO_FENCE,
                            "围栏报警",
                            String.format("%s越出%s围栏",
                                    deviceName,
                                    fence.getName()),
                            location.getLongitude(),
                            location.getLatitude(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
            );
        } catch (Exception e) {
            log.error("处理围栏越界异常: deviceId={}, fenceId={}",
                    location.getDeviceId(), fence.getId(), e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"处理围栏越界失败");
        }
    }

    private Long getGuardianId(String deviceId) {
        QueryWrapper<LocationData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("guardian_id")
                .eq("device_id", deviceId)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        LocationData locationData = locationDataMapper.selectOne(queryWrapper);
        return locationData != null ? locationData.getGuardian_id() : null;
    }
}