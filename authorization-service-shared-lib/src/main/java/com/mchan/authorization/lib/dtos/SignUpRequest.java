package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Defines a request that is used when Signing-Up into the Service.
 * The password fields that is required needs to be in plain-text.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    @ToString.Exclude
    private String password;
}
