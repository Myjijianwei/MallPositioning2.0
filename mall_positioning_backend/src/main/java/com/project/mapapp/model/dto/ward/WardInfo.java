package com.project.mapapp.model.dto.ward;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class WardInfo {
    /**
     * 被监护人 ID
     */
    private Long id;

    /**
     * 关联的用户 ID（监护人ID）
     */
    private Long userId;

    /**
     * 设备名称
     */
    private String deviceName;

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


}
