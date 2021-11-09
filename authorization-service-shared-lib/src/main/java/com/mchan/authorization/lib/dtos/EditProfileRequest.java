package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for editing a profile.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditProfileRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;

}
