package com.mchan.authorization.service.authorization.dao.mappers;

import com.mchan.authorization.lib.models.ApplicationGrant;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Persists directional grants and evaluates current active permissions.
 */
@Mapper
public interface ApplicationGrantsMapper {

    /**
     * Executes a directed grant persistence operation.
     */
    @Select("SELECT application_id FROM auth_db.applications WHERE application_id=#{id} AND is_active=true FOR UPDATE")
    Integer lockActiveApplication(int id);

    /**
     * Executes a directed grant persistence operation.
     */
    @Insert("INSERT INTO auth_db.application_grants(source_application_id,target_application_id,endpoint_id) "
        + "VALUES(#{sourceApplicationId},#{targetApplicationId},#{endpointId}) ON "
        + "CONFLICT(source_application_id,endpoint_id) DO UPDATE SET is_active=true,version=application_grants.version+1 "
        + "WHERE application_grants.is_active=false")
    int create(ApplicationGrant grant);

    /**
     * Executes a directed grant persistence operation.
     */
    @Select("SELECT g.grant_id AS \"grantId\", g.source_application_id AS \"sourceApplicationId\", g.target_application_id AS "
        + "\"targetApplicationId\", g.endpoint_id AS \"endpointId\", e.action, g.version, g.is_active AS active FROM "
        + "auth_db.application_grants g JOIN auth_db.application_endpoints e USING(endpoint_id) WHERE "
        + "g.target_application_id=#{targetId} AND g.grant_id>#{afterId} ORDER BY g.grant_id LIMIT #{limit}")
    List<ApplicationGrant> list(@org.apache.ibatis.annotations.Param("targetId") int targetId, @org.apache.ibatis.annotations.Param("limit") int limit,
                                    @org.apache.ibatis.annotations.Param("afterId") int afterId);

    /**
     * Executes a directed grant persistence operation.
     */
    @Select("SELECT g.grant_id AS \"grantId\", g.source_application_id AS \"sourceApplicationId\", g.target_application_id AS "
        + "\"targetApplicationId\", g.endpoint_id AS \"endpointId\", e.action, g.version, g.is_active AS active FROM "
        + "auth_db.application_grants g JOIN auth_db.application_endpoints e USING(endpoint_id) WHERE "
        + "g.target_application_id=#{targetId} AND g.source_application_id=#{sourceId} AND g.endpoint_id=#{endpointId}")
    ApplicationGrant get(@Param("sourceId") int sourceId, @Param("targetId") int targetId, @Param("endpointId") int endpointId);

    /**
     * Executes a directed grant persistence operation.
     */
    @Update("UPDATE auth_db.application_grants SET is_active=false,version=version+1 WHERE "
        + "target_application_id=#{targetId} AND grant_id=#{grantId} AND is_active=true")
    int revoke(@Param("targetId") int targetId, @Param("grantId") int grantId);

    /**
     * Executes a directed grant persistence operation.
     */
    @Select("SELECT g.grant_id AS \"grantId\", g.source_application_id AS \"sourceApplicationId\", g.target_application_id AS "
        + "\"targetApplicationId\", g.endpoint_id AS \"endpointId\", e.action, g.version, g.is_active AS active FROM "
        + "auth_db.application_grants g JOIN auth_db.application_endpoints e USING(endpoint_id) JOIN auth_db.applications "
        + "s ON s.application_id=g.source_application_id JOIN auth_db.applications t ON "
        + "t.application_id=g.target_application_id WHERE g.source_application_id=#{sourceId} AND "
        + "g.target_application_id=#{targetId} AND g.is_active=true AND e.is_active=true AND s.is_active=true AND "
        + "t.is_active=true ORDER BY e.action LIMIT 1001")
    List<ApplicationGrant> allowed(@Param("sourceId") int sourceId, @Param("targetId") int targetId);
}
