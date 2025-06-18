package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 被监护人信息表
 * @TableName ward
 */
@TableName(value ="ward")
@Data
public class Ward {
    /**
     * 被监护人 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户 ID
     */
    private Long userId;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}