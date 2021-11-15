package com.mchan.authorization.service.entities.dao.entities;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a Session that can be stored in the Persistence Layer.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionEntity {
    private int sessionId;
    private String accountId;
    private String sessionType;
    private Instant sessionTime;
}
