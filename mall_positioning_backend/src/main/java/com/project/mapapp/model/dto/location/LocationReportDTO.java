package com.project.mapapp.model.dto.location;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 位置上报DTO
 */
@Data
public class LocationReportDTO {
    private String deviceId;
    private Long guardianId;
    private Long wardId;
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度最小为-90")
    @DecimalMax(value = "90.0", message = "纬度最大为90")
    private BigDecimal latitude;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度最小为-180")
    @DecimalMax(value = "180.0", message = "经度最大为180")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0", message = "精度不能为负")
    private BigDecimal accuracy;
    private Date createTime;
}
