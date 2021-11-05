package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Request for retrieving a Profile.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProfileRequest {

    private String profileId;

}
