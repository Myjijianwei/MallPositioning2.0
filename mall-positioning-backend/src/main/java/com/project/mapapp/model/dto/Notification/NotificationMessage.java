package com.project.mapapp.model.dto.Notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class NotificationMessage {
    /**
     * 通知ID
     */
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
     * 申请状态
     */
    private String status;

    /**
     * 申请人姓名
     */
    private String userName;

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

}
