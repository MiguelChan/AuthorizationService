package com.mchan.authorization.service.authorization.dao.mappers;

import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Client credential persistence with serialized rotation and no plaintext storage.
 */
@Mapper
public interface ClientCredentialsMapper {

    /**
     * Executes the credential persistence operation.
     */
    @Select("SELECT application_id FROM auth_db.applications WHERE application_id=#{id} AND is_active=true FOR UPDATE")
    Integer lockActiveApplication(int id);

    /**
     * Executes the credential persistence operation.
     */
    @Select("SELECT c.application_id AS \"applicationId\", c.client_id AS \"clientId\", c.secret_hash AS \"secretHash\", "
        + "c.version FROM auth_db.client_credentials c JOIN auth_db.applications a USING(application_id) WHERE "
        + "c.client_id=#{clientId} AND c.revoked_at IS NULL AND a.is_active=true")
    ClientCredentialEntity findActive(String clientId);

    /**
     * Executes the credential persistence operation.
     */
    @Select("SELECT c.application_id AS \"applicationId\", c.client_id AS \"clientId\", c.secret_hash AS \"secretHash\", "
        + "c.version FROM auth_db.client_credentials c WHERE c.application_id=#{id}")
    ClientCredentialEntity findByApplication(int id);

    /**
     * Executes the credential persistence operation.
     */
    @Insert("INSERT INTO auth_db.client_credentials(application_id,client_id,secret_hash) "
        + "VALUES(#{applicationId},#{clientId},#{secretHash}) ON CONFLICT(application_id) DO UPDATE SET "
        + "secret_hash=EXCLUDED.secret_hash,version=client_credentials.version+1,revoked_at=NULL,issued_at=CURRENT_TIMESTAMP")
    void save(ClientCredentialEntity credentials);

    /**
     * Executes the credential persistence operation.
     */
    @Update("UPDATE auth_db.client_credentials SET revoked_at=CURRENT_TIMESTAMP,version=version+1 WHERE "
        + "application_id=#{id} AND revoked_at IS NULL")
    int revoke(int id);

    /**
     * Upgrades a verified legacy hash only while its exact version and hash remain current.
     */
    @Update("UPDATE auth_db.client_credentials SET secret_hash=#{newHash} WHERE application_id=#{id} AND "
        + "version=#{version} AND secret_hash=#{oldHash} AND revoked_at IS NULL")
    int upgradeHash(@org.apache.ibatis.annotations.Param("id") int id, @org.apache.ibatis.annotations.Param("version") long version,
                    @org.apache.ibatis.annotations.Param("oldHash") String oldHash, @org.apache.ibatis.annotations.Param("newHash") String newHash);
}
