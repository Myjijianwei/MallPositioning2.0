package com.project.mapapp.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.mapper.WardMapper;
import com.project.mapapp.model.dto.ward.WardInfo;
import com.project.mapapp.model.entity.Ward;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/ward")
@Slf4j
public class WardController {

    private final WardMapper wardMapper;

    public WardController(WardMapper wardMapper) {
        this.wardMapper = wardMapper;
    }

    @GetMapping("/getWardInfo")
    public BaseResponse<WardInfo> getWardInfo(String wardId, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(wardId), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(new WardInfo());
    }

    @PostMapping("/updateRelationship")
    public BaseResponse<String> updateWardRelationship(String wardId,String guardianId,String relationship, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(wardId), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isEmpty(guardianId), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isEmpty(relationship), ErrorCode.PARAMS_ERROR);
        Ward ward = new Ward();
        ward.setId(Long.valueOf(wardId));
        ward.setUserId(Long.valueOf(guardianId));
        ward.setRelationship(relationship);
        QueryWrapper<Ward> wardQueryWrapper = new QueryWrapper<>();
        wardQueryWrapper.eq("id", wardId);
        wardQueryWrapper.eq("userId", guardianId);
        wardMapper.update(ward, wardQueryWrapper);
        return ResultUtils.success("更新成功");
    }
}
