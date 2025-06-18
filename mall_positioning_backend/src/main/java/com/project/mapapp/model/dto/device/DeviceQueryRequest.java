package com.project.mapapp.model.dto.device;

import com.baomidou.mybatisplus.annotation.TableId;
import com.project.mapapp.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DeviceQueryRequest extends PageRequest implements Serializable {
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
     * 绑定的监护人ID
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
}