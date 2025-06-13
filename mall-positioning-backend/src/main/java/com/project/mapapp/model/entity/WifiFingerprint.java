package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * WIFI指纹表
 * @TableName wifi_fingerprint
 */
@TableName(value ="wifi_fingerprint")
@Data
public class WifiFingerprint {
    /**
     * WIFI BSSID
     */
    @TableId
    private String bssid;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 信号强度分布直方图
     */
    private Object rssi_histogram;

    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;
}