package com.mchan.authorization.lib.dtos;

import com.mchan.authorization.lib.models.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class GetProfileResponse extends BaseResponse {

    private Profile profile;

    /**
     * .
     *
     * @param message .
     *
     * @param profile .
     */
    @Builder
    public GetProfileResponse(String message, Profile profile) {
        super(message);
        this.profile = profile;
    }

}
