package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 位置数据表
 * @TableName location_data_test
 */
@TableName(value ="location_data")
@Data
public class LocationData {
    /**
     * 位置记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID/用户标识
     */
    private String device_id;

    /**
     * 纬度(-90~90)
     */
    private BigDecimal latitude;

    /**
     * 经度(-180~180)
     */
    private BigDecimal longitude;

    /**
     * 定位精度(米)
     */
    private BigDecimal accuracy;

    /**
     * 定位时间
     */
    private Date timestamp;

    /**
     * 关联监护人ID
     */
    private Long guardian_id;

    /**
     * 记录时间
     */
    private LocalDateTime create_time;
}