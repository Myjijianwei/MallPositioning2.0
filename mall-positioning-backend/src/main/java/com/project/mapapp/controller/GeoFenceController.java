package com.project.mapapp.controller;

import cn.hutool.core.util.ObjUtil;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.model.dto.geofence.GeoFenceCreateRequest;
import com.project.mapapp.model.dto.geofence.GeoFenceUpdateRequest;
import com.project.mapapp.model.entity.GeoFence;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.service.GeoFenceService;
import com.project.mapapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/geo-fence")
@RequiredArgsConstructor
public class GeoFenceController {
    private final GeoFenceService geoFenceService;
    private final UserService userService;

    @PostMapping("/create")
    public BaseResponse<Boolean> createGeoFence(
            @RequestBody GeoFenceCreateRequest geoFenceCreateRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(geoFenceCreateRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        geoFenceCreateRequest.setUserId(String.valueOf(loginUser.getId()));
        boolean result = geoFenceService.createGeoFence(geoFenceCreateRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    public BaseResponse<List<GeoFence>> listFences(
            @RequestParam String deviceId,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        List<GeoFence> geoFences = geoFenceService.listFences(deviceId, loginUser.getId());
        return ResultUtils.success(geoFences);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateGeoFence(
            @Valid @RequestBody GeoFenceUpdateRequest updateRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isEmpty(updateRequest), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(updateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "围栏ID不能为空");
        ThrowUtils.throwIf(updateRequest.getCoordinates() != null && updateRequest.getCoordinates().size() < 3,
                ErrorCode.PARAMS_ERROR, "围栏至少需要3个坐标点");

        // 权限校验
        User loginUser = userService.getLoginUser(request);

        // 执行更新
        boolean result = geoFenceService.updateGeoFence(updateRequest, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteGeoFence(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        boolean result = geoFenceService.deleteGeoFence(id, loginUser.getId());
        return ResultUtils.success(result);
    }
}