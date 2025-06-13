package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 商场区域表
 * @TableName mall_area
 */
@TableName(value ="mall_area")
@Data
public class MallArea {
    /**
     * 区域ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 区域坐标（GeoJSON格式）
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