package com.project.mapapp.controller;


import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.annotation.AuthCheck;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.constant.UserConstant;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.mapper.UserMapper;
import com.project.mapapp.mapper.WardMapper;
import com.project.mapapp.model.dto.device.DeviceBindRequest;
import com.project.mapapp.model.dto.device.DeviceQueryRequest;
import com.project.mapapp.model.dto.device.DeviceUpdateRequest;
import com.project.mapapp.model.dto.device.DeviceInfo;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.model.entity.Ward;
import com.project.mapapp.model.enums.ApplicationStatus;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WardMapper wardMapper;

    /**
     * 获取所有设备
     * @return
     */
    @GetMapping("/listAllDevice")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<DeviceQueryRequest>> listAllDevice() {
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Device> devices = deviceMapper.selectList(queryWrapper);
        List<DeviceQueryRequest> deviceQueryRequests = devices.stream().map(device -> {
            DeviceQueryRequest deviceQueryRequest = new DeviceQueryRequest();
            BeanUtils.copyProperties(device, deviceQueryRequest);
            return deviceQueryRequest;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(devices, deviceQueryRequests);
        return ResultUtils.success(deviceQueryRequests);
    }


    @GetMapping("/listDeviceById")
    @Deprecated
    public BaseResponse<List<DeviceQueryRequest>> listDeviceById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        List<Device> devices = deviceMapper.selectList(queryWrapper);
        List<DeviceQueryRequest> deviceQueryRequests = devices.stream().map(device -> {
            DeviceQueryRequest deviceQueryRequest = new DeviceQueryRequest();
            BeanUtils.copyProperties(device, deviceQueryRequest);
            return deviceQueryRequest;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(devices, deviceQueryRequests);
        return ResultUtils.success(deviceQueryRequests);
    }

    /**
     * 获取监护人绑定的所有被监护人的设备信息
     * @param guardianId
     * @param request
     * @return
     */
    @GetMapping("/getWardDeviceByGuardId/{guardianId}")
    public BaseResponse<List<DeviceInfo>> getWardDevice(@PathVariable String guardianId, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isEmpty(guardianId),ErrorCode.PARAMS_ERROR);
        QueryWrapper<Application> applicationQueryWrapper = new QueryWrapper<>();
        applicationQueryWrapper.eq("guardian_id", guardianId);
        List<Application> applications = applicationMapper.selectList(applicationQueryWrapper);
        List<DeviceInfo> deviceQueryRequestList=new ArrayList<>();
        for (Application application : applications) {
            //只有通过的申请才是为绑定成功
            if(application.getStatus().equals(ApplicationStatus.APPROVED.getCode())){
                DeviceInfo wardDeviceInfo = new DeviceInfo();

                Device device = deviceMapper.selectById(application.getWard_device_id());
                wardDeviceInfo.setDeviceId(device.getId());
                wardDeviceInfo.setDeviceName(device.getName());
                wardDeviceInfo.setWardId(device.getUser_id());
                wardDeviceInfo.setDevice_description(device.getDevice_description());
                wardDeviceInfo.setGuardianId(Long.valueOf(application.getGuardian_id()));


                Ward ward = wardMapper.selectById(wardDeviceInfo.getWardId());
                BeanUtils.copyProperties(ward, wardDeviceInfo);


                wardDeviceInfo.setWardName(userMapper.selectById(wardDeviceInfo.getWardId()).getUserName());
                wardDeviceInfo.setCreated_at(application.getUpdated_at());
                deviceQueryRequestList.add(wardDeviceInfo);
            }
        }
        return ResultUtils.success(deviceQueryRequestList);
    }

    /**
     * 绑定设备
     * @param device
     * @param request
     * @return
     */
    @PostMapping("/bindDevice")
    public BaseResponse<String> bindDevice(@RequestBody DeviceBindRequest device, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || device == null, ErrorCode.PARAMS_ERROR);
        Boolean isBindSuccess = deviceService.bindDevice(device.getDeviceId(), loginUser.getId(), loginUser.getEmail());
        ThrowUtils.throwIf(!isBindSuccess, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("绑定成功");
    }

    /**
     * 获取当前用户的设备
     * @param request
     * @return
     */
    @GetMapping("/getDeviceById")
    public BaseResponse<Device> getDeviceById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        Device device = deviceMapper.selectOne(queryWrapper);
        return ResultUtils.success(device);
    }

    /**
     * 获取当前用户的设备
     * @param request
     * @return
     */
    @GetMapping("/getDeviceById/{guardianId}")
    public BaseResponse<Device> getDeviceById(@PathVariable String guardianId, HttpServletRequest request) {
        ThrowUtils.throwIf(guardianId == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", guardianId);
        Device device = deviceMapper.selectOne(queryWrapper);
        return ResultUtils.success(device);
    }

    @GetMapping("/getMySelfDeviceInfo")
    public BaseResponse<DeviceInfo> getMySelfDeviceInfo(int id,HttpServletRequest request) {
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userService.getLoginUser(request).getId());
        Device devices = deviceMapper.selectOne(queryWrapper);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(devices.getId());
        deviceInfo.setDeviceName(devices.getName());
        QueryWrapper<Application> applicationQueryWrapper = new QueryWrapper<>();
        applicationQueryWrapper.eq("ward_device_id", devices.getId());
        Application application = applicationMapper.selectOne(applicationQueryWrapper);
        deviceInfo.setGuardianId(Long.valueOf(application.getGuardian_id()));
        deviceInfo.setGuardianName(userMapper.selectById(application.getGuardian_id()).getUserName());
        return ResultUtils.success(deviceInfo);
    }

    /**
     * 更新设备信息
     * @param deviceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/updateDevice")
    public BaseResponse<String> updateDevice(@RequestBody DeviceUpdateRequest deviceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(deviceUpdateRequest), ErrorCode.PARAMS_ERROR);
        boolean is=deviceService.updateDevice(deviceUpdateRequest);
        if(is){
            return ResultUtils.success("更新成功！");
        }
        return ResultUtils.success("更新失败");
    }

    /**
     * 获取被监护人对应的监护人的设备信息
     * @param wardId
     * @param request
     * @return
     */
    @GetMapping("/getGuardianDevices")
    public BaseResponse<List<Device>> getGuardianDevices(@RequestParam int wardId, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(wardId), ErrorCode.PARAMS_ERROR);

        // 1. 获取被监护人自己的设备
        QueryWrapper<Device> wardDeviceQuery = new QueryWrapper<>();
        wardDeviceQuery.eq("user_id", wardId);
        Device wardDevice = deviceMapper.selectOne(wardDeviceQuery);
        if (wardDevice == null) {
            return ResultUtils.success(new ArrayList<>());
        }

        // 2. 获取所有关联的监护人ID
        QueryWrapper<Application> applicationQuery = new QueryWrapper<>();
        applicationQuery.eq("ward_device_id", wardDevice.getId());
        List<Application> applications = applicationMapper.selectList(applicationQuery);

        Set<String> guardianIds = applications.stream()
                .map(Application::getGuardian_id)
                .collect(Collectors.toSet());

        // 3. 获取所有监护人的设备
        List<Device> guardianDevices = new ArrayList<>();
        if (!guardianIds.isEmpty()) {
            QueryWrapper<Device> guardianDeviceQuery = new QueryWrapper<>();
            guardianDeviceQuery.in("user_id", guardianIds);
            guardianDevices = deviceMapper.selectList(guardianDeviceQuery);
        }
        for (Device device : guardianDevices) {
            QueryWrapper<Ward> wardQuery = new QueryWrapper<>();
            wardQuery.eq("userId", device.getUser_id());
            wardQuery.eq("id", wardId);
            Ward ward = wardMapper.selectOne(wardQuery);
            device.setRelationship(ward.getRelationship());
        }

        return ResultUtils.success(guardianDevices);
    }


}

