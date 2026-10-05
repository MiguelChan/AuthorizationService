package com.mchan.authorization.service.authorization.dao.mappers;

import com.mchan.authorization.service.authorization.dao.entities.OauthPermissionEntity;
import com.mchan.authorization.service.authorization.dao.entities.OauthTokenEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Persists opaque tokens and checks revocation, audience credentials and grant versions online.
 */
@Mapper
public interface OauthTokensMapper {

    /**
     * Executes an OAuth token persistence operation.
     */
    @Insert("INSERT INTO "
        + "auth_db.oauth_tokens(token_hash,source_application_id,target_application_id,source_credential_version,target_credential_version,issuer,issued_at,expires_at) "
        + "VALUES(#{tokenHash},#{sourceId},#{targetId},#{sourceCredentialVersion},#{targetCredentialVersion},#{issuer},#{issuedAt},#{expiresAt})")
    void create(OauthTokenEntity token);

    /**
     * Executes an OAuth token persistence operation.
     */
    @Insert("INSERT INTO auth_db.oauth_token_permissions(token_hash,grant_id,grant_version) "
        + "VALUES(#{hash},#{grantId},#{version})")
    void addPermission(@Param("hash") String hash, @Param("grantId") int grantId, @Param("version") long version);

    /**
     * Executes an OAuth token persistence operation.
     */
    @Select("SELECT t.token_hash AS \"tokenHash\", t.source_application_id AS \"sourceId\", t.target_application_id AS "
        + "\"targetId\", t.source_credential_version AS \"sourceCredentialVersion\", t.target_credential_version AS "
        + "\"targetCredentialVersion\", t.issuer, t.issued_at AS \"issuedAt\", t.expires_at AS \"expiresAt\" FROM "
        + "auth_db.oauth_tokens t JOIN auth_db.applications s ON s.application_id=t.source_application_id JOIN "
        + "auth_db.applications a ON a.application_id=t.target_application_id JOIN auth_db.client_credentials sc ON "
        + "sc.application_id=s.application_id JOIN auth_db.client_credentials ac ON ac.application_id=a.application_id "
        + "WHERE t.token_hash=#{hash} AND t.revoked_at IS NULL AND t.expires_at>CURRENT_TIMESTAMP AND s.is_active=true "
        + "AND a.is_active=true AND sc.revoked_at IS NULL AND ac.revoked_at IS NULL AND "
        + "sc.version=t.source_credential_version AND ac.version=t.target_credential_version")
    OauthTokenEntity findActive(String hash);

    /**
     * Executes an OAuth token persistence operation.
     */
    @Select("SELECT e.endpoint_id AS \"endpointId\", e.action, e.http_method AS \"httpMethod\", e.path FROM "
        + "auth_db.oauth_token_permissions p JOIN auth_db.application_grants g USING(grant_id) JOIN "
        + "auth_db.application_endpoints e USING(endpoint_id) JOIN auth_db.oauth_tokens t ON t.token_hash=p.token_hash "
        + "WHERE p.token_hash=#{hash} AND g.is_active=true AND e.is_active=true AND g.version=p.grant_version AND "
        + "g.source_application_id=t.source_application_id AND g.target_application_id=t.target_application_id ORDER BY "
        + "e.action")
    List<OauthPermissionEntity> permissions(String hash);

    /**
     * Executes an OAuth token persistence operation.
     */
    @Select("SELECT client_id FROM auth_db.client_credentials WHERE application_id=#{id}")
    String clientId(int id);

    /**
     * Executes an OAuth token persistence operation.
     */
    @Update("UPDATE auth_db.oauth_tokens SET revoked_at=CURRENT_TIMESTAMP WHERE token_hash=#{hash} AND "
        + "source_application_id=#{sourceId} AND revoked_at IS NULL")
    int revoke(@Param("hash") String hash, @Param("sourceId") int sourceId);
}
