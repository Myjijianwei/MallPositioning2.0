package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 通知表
 * @TableName notification
 */
@TableName(value ="notification")
@Data
public class Notification {
    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String user_id;

    /**
     * 申请ID
     */
    private String application_id;

    /**
     * 通知内容
     */
    private String message;

    /**
     * 是否已读（0: 未读, 1: 已读）
     */
    private Integer is_read;

    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;
}