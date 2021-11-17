package com.mchan.authorization.lib.dtos;

import com.mchan.authorization.lib.models.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request used for updating an {@link Application}.
 */
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationRequest {

    private Application application;
    private int applicationId;

}
