package com.mchan.authorization.lib.dtos;

import com.mchan.authorization.lib.models.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Simple request used for creating an {@link Application}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateApplicationRequest {
    private Application application;
    private String profileId;
}
