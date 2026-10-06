package com.mchan.authorization.service.authorization.dao.mappers;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Persists owner-managed authorization metadata.
 */
@Mapper
public interface ApplicationEndpointsMapper {

    /**
     * Executes the catalog persistence operation.
     */
    @Select("SELECT application_id FROM auth_db.applications WHERE application_id=#{id} AND is_active=true FOR UPDATE")
    Integer lockActiveApplication(int id);

    /**
     * Executes the catalog persistence operation.
     */
    @Insert("INSERT INTO auth_db.application_endpoints(application_id,http_method,path,action,description) "
        + "VALUES(#{applicationId},#{httpMethod},#{path},#{action},#{description})")
    @Options(useGeneratedKeys = true, keyProperty = "endpointId", keyColumn = "endpoint_id")
    void create(ApplicationEndpoint endpoint);

    /**
     * Executes the catalog persistence operation.
     */
    @Select("SELECT endpoint_id AS \"endpointId\", application_id AS \"applicationId\", http_method AS \"httpMethod\", path, "
        + "action, description, is_active AS active FROM auth_db.application_endpoints WHERE application_id=#{appId} "
        + "AND endpoint_id>#{afterId} ORDER BY endpoint_id LIMIT #{limit}")
    List<ApplicationEndpoint> list(@org.apache.ibatis.annotations.Param("appId") int appId, @org.apache.ibatis.annotations.Param("limit") int limit,
                                    @org.apache.ibatis.annotations.Param("afterId") int afterId);

    /**
     * Executes the catalog persistence operation.
     */
    @Select("SELECT endpoint_id AS \"endpointId\", application_id AS \"applicationId\", http_method AS \"httpMethod\", path, "
        + "action, description, is_active AS active FROM auth_db.application_endpoints WHERE application_id=#{appId} AND "
        + "endpoint_id=#{endpointId}")
    ApplicationEndpoint get(@Param("appId") int appId, @Param("endpointId") int endpointId);

    /**
     * Executes the catalog persistence operation.
     */
    @Update("UPDATE auth_db.application_endpoints SET description=#{description} WHERE application_id=#{applicationId} AND "
        + "endpoint_id=#{endpointId} AND is_active=true")
    int updateDescription(ApplicationEndpoint endpoint);

    /**
     * Executes the catalog persistence operation.
     */
    @Update("UPDATE auth_db.application_endpoints SET is_active=false WHERE application_id=#{appId} AND "
        + "endpoint_id=#{endpointId} AND is_active=true")
    int deactivate(@Param("appId") int appId, @Param("endpointId") int endpointId);

    /**
     * Enforces the per-application endpoint quota while its application row is locked.
     */
    @Select("SELECT count(*) FROM auth_db.application_endpoints WHERE application_id=#{id}")
    int count(int id);
}
