package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response used for an {@link com.mchan.authorization.lib.models.Application} update.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateApplicationResponse extends BaseResponse {

    private boolean success;

    /**
     * .
     *
     * @param message .
     *
     * @param success .
     */
    @Builder(toBuilder = true)
    public UpdateApplicationResponse(String message, boolean success) {
        super(message);
        this.success = success;
    }

}
