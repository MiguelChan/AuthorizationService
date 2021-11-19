package com.mchan.authorization.service.spring.security;

import com.mchan.authorization.lib.models.Profile;
import java.util.ArrayList;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * The Spring {@link org.springframework.security.core.Authentication} for this App.
 */
@Getter
@Setter
public class EntitiesAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final Profile profile;

    /**
     * .
     *
     * @param principal .
     *
     * @param credentials .
     *
     * @param profile .
     */
    @Builder
    public EntitiesAuthenticationToken(Object principal,
                                       Object credentials,
                                       Profile profile) {
        super(principal, credentials, new ArrayList<>());
        this.profile = profile;
    }

}
