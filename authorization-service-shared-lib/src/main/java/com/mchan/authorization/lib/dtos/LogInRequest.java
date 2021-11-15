package com.mchan.authorization.lib.dtos;

import com.mchan.authorization.lib.models.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The request for performing a Log In.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LogInRequest {

    private AuthenticationType authType;
    private String userName;
    private String password;

}
