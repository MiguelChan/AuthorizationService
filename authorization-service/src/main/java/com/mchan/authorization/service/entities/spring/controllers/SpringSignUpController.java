package com.mchan.authorization.service.entities.spring.controllers;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.components.SignUpComponent;
import com.mchan.authorization.service.entities.controllers.SignUpController;
import com.mchan.authorization.service.exceptions.SignUpException;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The sign up controller. Used to register new Users into our System.
 */
@Log4j2
@RequestMapping(value = "/", produces = "application/json")
@RestController
public class SpringSignUpController implements SignUpController {

    private final SignUpComponent signUpComponent;

    @Autowired
    public SpringSignUpController(SignUpComponent signUpComponent) {
        this.signUpComponent = signUpComponent;
    }

    @Override
    @PostMapping(value = "/sign-up", consumes = "application/json")
    public SignUpResponse signUp(@RequestBody SignUpRequest request) {
        try {
            Optional<String> profileIdOpt = signUpComponent.signUp(request);
            return SignUpResponse.builder()
                .profileId(profileIdOpt.orElse(""))
                .build();
        } catch (SignUpException signUpException) {
            log.error(signUpException);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                signUpException.getMessage(),
                signUpException
            );
        } catch (Exception e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
