package com.mchan.authorization.lib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The overall Entity in the project.
 * An {@link Entity} is meant to be from a single {@link Profile}.
 * This means that each {@link Profile} will tie back to a single {@link Entity}.
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Entity {
    private String entityId;
    private Profile profile;
}
