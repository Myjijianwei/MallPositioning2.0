package com.project.mapapp.model.dto.geofence;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class GeoFenceUpdateRequest {
    @NotBlank
    private String id;

    @Size(max = 50)
    private String name;

    @Size(min = 3, message = "至少需要3个坐标点")
    private List<List<Double>> coordinates;
}