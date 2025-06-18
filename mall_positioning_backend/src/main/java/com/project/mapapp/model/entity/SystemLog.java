package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统日志表
 * @TableName system_log
 */
@TableName(value ="system_log")
@Data
public class SystemLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private String user_id;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 操作详情
     */
    private String details;

    /**
     * 操作时间
     */
    private Date created_at;
}