package com.project.mapapp.model.dto.alert;

import com.project.mapapp.model.enums.AlertStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AlertBatchUpdateDTO {
    @NotNull(message = "警报ID列表不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private AlertStatus status;
}
