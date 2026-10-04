package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A Response used when an {@link com.mchan.authorization.lib.models.Application} gets created.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateApplicationResponse extends BaseResponse {

    private int applicationId;
    private String clientId;
    @lombok.ToString.Exclude
    private String clientSecret;

    /**
     * .
     *
     * @param message .
     *
     * @param applicationId .
     */
    @Builder
    public CreateApplicationResponse(String message, int applicationId, String clientId, String clientSecret) {
        super(message);
        this.applicationId = applicationId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

}
