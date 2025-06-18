package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 设备表
 * @TableName device
 */
@TableName(value ="device")
@Data
public class Device {
    /**
     * 设备ID
     */
    @TableId
    private String id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 绑定的用户人ID
     */
    private Long user_id;


    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;

    /**
     * 设备状态（0未绑定，1已绑定）
     */
    private Integer status;

    /**
     * 设备描述信息，用于区分不同被监护人的设备
     */
    private String device_description;

    @TableField(exist = false)
    private Date bindData;

    @TableField(exist = false)
    private String relationship;
}