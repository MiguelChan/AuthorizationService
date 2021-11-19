package com.mchan.authorization.service.entities.spring.facade;

import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * .
 */
@Log4j2
@Component
public class AuthenticationFacade {

    /**
     * Retrieves the current {@link EntitiesAuthenticationToken} from the SpringSecurity Context.
     * Throws a {@link RuntimeException} if the provided token is invalid.
     *
     * @return .
     */
    public EntitiesAuthenticationToken getAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof EntitiesAuthenticationToken) {
            return (EntitiesAuthenticationToken) authentication;
        }
        log.info("ReceivedClassName: {}", authentication.getClass().getName());
        throw new RuntimeException("???????");
    }

}
