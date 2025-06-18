package com.project.mapapp.model.dto.device;

import com.baomidou.mybatisplus.annotation.TableId;
import com.project.mapapp.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DeviceInfo extends PageRequest implements Serializable {
    /**
     * 设备ID
     */
    @TableId
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 被监护人ID
     */
    private Long wardId;

    /**
     * 监护人 ID
     */
    private Long guardianId;

    private String guardianName;

    /**
     * 被监护人姓名
     */
    private String wardName;

    /**
     * 被监护人年龄
     */
    private Integer userAge;

    /**
     * 紧急联系人信息
     */
    private String emergencyContact;

    /**
     * 与监护人的关系
     */
    private String relationship;

    /**
     * 绑定时间
     */
    private Date created_at;

    /**
     * 设备描述信息，用于区分不同被监护人的设备
     */
    private String device_description;
}