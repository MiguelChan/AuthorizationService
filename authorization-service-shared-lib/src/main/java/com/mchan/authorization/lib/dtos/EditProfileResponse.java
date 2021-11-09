package com.mchan.authorization.lib.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The response returned when the profile has been edited.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EditProfileResponse extends BaseResponse {

    private boolean success;

    /**
     * .
     *
     * @param message .
     *
     * @param success .
     */
    @Builder
    public EditProfileResponse(String message, boolean success) {
        super(message);
        this.success = success;
    }

}
