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

    private boolean success;

    /**
     * .
     *
     * @param message .
     *
     * @param success .
     */
    @Builder
    public CreateApplicationResponse(String message, boolean success) {
        super(message);
        this.success = success;
    }

}
