package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Defines a simple Response.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class PingResponse extends BaseResponse {

    private boolean healthy;

    /**
     * .
     *
     * @param message .
     *
     * @param healthy .
     */
    @Builder
    public PingResponse(String message, boolean healthy) {
        super(message);
        this.healthy = healthy;
    }

}
