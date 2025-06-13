package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 电子围栏表
 * @TableName geo_fence
 */
@TableName(value ="geo_fence")
@Data
public class GeoFence {
    /**
     * 围栏ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 监护人ID
     */
    private String user_id;

    /**
     * 设备ID
     */
    private String device_id;

    /**
     * 围栏名称
     */
    private String name;

    /**
     * 围栏坐标（GeoJSON格式）
     */
    private Object coordinates;

    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;
}