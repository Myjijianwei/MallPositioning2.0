package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 监护人与被监护人关系表
 * @TableName guardian_ward_relation
 */
@TableName(value ="guardian_ward_relation")
@Data
public class GuardianWardRelation {
    /**
     * 关系 ID，可使用 UUID 生成
     */
    @TableId
    private String id;

    /**
     * 监护人的用户 ID
     */
    private String guardian_user_id;

    /**
     * 被监护人的用户 ID
     */
    private String ward_user_id;

    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;
}