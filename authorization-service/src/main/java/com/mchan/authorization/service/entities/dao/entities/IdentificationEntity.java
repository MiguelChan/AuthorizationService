package com.mchan.authorization.service.entities.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines the "Entity" that will be used for validating data.
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentificationEntity {
    private String entityId;
    private String profileId;
}
