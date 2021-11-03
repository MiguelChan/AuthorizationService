package com.mchan.authorization.service.entities.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a Profile Entity.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class ProfileEntity {

    private String profileId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;

}
