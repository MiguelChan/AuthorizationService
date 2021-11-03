package com.mchan.authorization.lib.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a Profile in our domain.
 * In this case, a Profile is the "last" and "final" (The EndUser) of the Application.
 * e.g. In the Bakery use-case the Profile will match to an EndUser of the App like the manager
 * or the accountant.
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Profile {
    private String profileId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private List<Account> accounts;
}
