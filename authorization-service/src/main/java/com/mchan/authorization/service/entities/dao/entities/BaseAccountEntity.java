package com.mchan.authorization.service.entities.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a Base Account Entity.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class BaseAccountEntity {

    protected String accountId;
    protected String profileId;
    protected String accountType;

}
