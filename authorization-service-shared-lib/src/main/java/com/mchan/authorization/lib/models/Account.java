package com.mchan.authorization.lib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines an Account in our System.
 * An {@link Account} will hold a {@link Profile} in the System, and each profile will be able
 * to tie back into an Account or Multiple ones.
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String accountId;
    private Profile profile;
    private AccountType accountType;
}
