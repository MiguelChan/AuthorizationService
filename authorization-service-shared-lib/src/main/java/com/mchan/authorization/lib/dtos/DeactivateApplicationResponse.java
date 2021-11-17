package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * .
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeactivateApplicationResponse extends BaseResponse {

    private boolean success;

    /**
     * .
     *
     * @param message .
     *
     * @param success .
     */
    @Builder(toBuilder = true)
    public DeactivateApplicationResponse(String message, boolean success) {
        super(message);
        this.success = success;
    }
}
