package com.mchan.authorization.service.authorization.dao.mappers;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * Prunes approved audit state without waiting for rows used by another transaction.
 */
@Mapper
public interface RetentionMapper {

    /**
     * Deletes at most 2,000 expired tokens and their cascading permission snapshots.
     */
    @Delete("DELETE FROM auth_db.oauth_tokens WHERE token_hash IN (SELECT token_hash FROM auth_db.oauth_tokens "
        + "WHERE expires_at<=CURRENT_TIMESTAMP ORDER BY expires_at,token_hash LIMIT 2000 FOR UPDATE SKIP LOCKED)")
    int expiredTokens();

    /**
     * Deletes at most 1,000 login records strictly older than the approved 30-day window.
     */
    @Delete("DELETE FROM auth_db.sessions WHERE session_id IN (SELECT session_id FROM auth_db.sessions "
        + "WHERE session_time<CURRENT_TIMESTAMP-INTERVAL '30 days' ORDER BY session_time,session_id LIMIT 1000 FOR UPDATE SKIP LOCKED)")
    int oldLoginHistory();
}
