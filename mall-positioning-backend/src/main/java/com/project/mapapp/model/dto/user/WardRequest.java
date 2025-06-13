package com.project.mapapp.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class WardRequest {
    /**
     * 被监护人 ID
     */
    private Long id;

    private String deviceId;

    /**
     * 关联的用户 ID
     */
    private Long userId;

    private String name;

    private String deviceName;
}
