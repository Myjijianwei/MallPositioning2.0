package com.project.mapapp.model.dto.device;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DeviceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;
    private String name;
    private Long userId;
    private String deviceDescription;
    private Integer status;

    // 以下字段仅在前端展示时使用
    private Long wardId;
    private Long guardianId;
    private String guardianName;
    private String wardName;
    private Integer userAge;
    private String emergencyContact;
    private String relationship;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    private Date updatedAt;
}
