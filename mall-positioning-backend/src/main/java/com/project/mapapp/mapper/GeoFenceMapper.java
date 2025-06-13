package com.project.mapapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.mapapp.model.entity.GeoFence;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author jjw
* @description 针对表【geo_fence(电子围栏表)】的数据库操作Mapper
* @createDate 2025-03-03 14:31:28
* @Entity com.example.mapapp.model.entity.GeoFence
*/
public interface GeoFenceMapper extends BaseMapper<GeoFence> {
    @Select("SELECT * FROM geo_fence WHERE device_id = #{deviceId}")
    List<GeoFence> selectByDeviceId(@Param("deviceId") String deviceId);
}




