package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The response provided once the User is logged in.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LogInResponse extends BaseResponse {

    private boolean loggedIn;

    /**
     * .
     *
     * @param message .
     *
     * @param loggedIn .
     */
    @Builder
    public LogInResponse(String message, boolean loggedIn) {
        super(message);
        this.loggedIn = loggedIn;
    }
}
