package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName application
 */
@TableName(value ="application")
@Data
public class Application {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String guardian_id;

    /**
     * 
     */
    private String ward_device_id;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;
}