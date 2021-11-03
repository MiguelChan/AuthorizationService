package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Defines the SignUp Response.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class SignUpResponse extends BaseResponse {

    private String profileId;

    /**
     * .
     *
     * @param message .
     *
     * @param profileId .
     */
    @Builder
    public SignUpResponse(String message, String profileId) {
        super(message);
        this.profileId = profileId;
    }

}
