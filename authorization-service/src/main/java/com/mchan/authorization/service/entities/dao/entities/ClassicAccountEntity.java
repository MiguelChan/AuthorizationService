package com.mchan.authorization.service.entities.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Defines a Classic Account Entity.
 * A ClassicAccount is meant to be handled as a traditional Username/Password Schema.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClassicAccountEntity extends BaseAccountEntity {

    private String email;
    private String password;

    /**
     * .
     *
     * @param email .
     *
     * @param password .
     *
     *
     * @param accountType .
     */
    @Builder(toBuilder = true)
    public ClassicAccountEntity(String accountId,
                                String email,
                                String profileId,
                                String password,
                                String accountType) {
        super(accountId, profileId, accountType);
        this.email = email;
        this.password = password;

    }

}
