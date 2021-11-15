package com.mchan.authorization.service.entities.spring.controllers;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.dtos.LogInResponse;
import com.mchan.authorization.service.entities.components.LogInComponent;
import com.mchan.authorization.service.entities.controllers.LogInController;
import com.mchan.authorization.service.exceptions.NotAuthorizedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Spring {@link LogInController}.
 */
@Log4j2
@RestController
@RequestMapping("/")
public class SpringLogInController implements LogInController {

    private final LogInComponent logInComponent;

    @Autowired
    public SpringLogInController(LogInComponent logInComponent) {
        this.logInComponent = logInComponent;
    }

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    @PostMapping("/authenticate")
    @Override
    public LogInResponse logIn(@RequestBody LogInRequest request) {
        try {
            boolean loggedIn = logInComponent.logIn(request);
            return LogInResponse.builder()
                .loggedIn(loggedIn)
                .build();
        } catch (NotAuthorizedException noa) {
            log.error(noa);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, noa.getMessage(), noa);
        } catch (Exception e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
