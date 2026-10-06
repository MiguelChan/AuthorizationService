package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.LogInRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * .
 */
@RestController
@RequestMapping("/")
@Tag(name = "Authentication")
public class LogInController {

    /**
     * .
     *
     * @param logInRequest .
     */
    @Operation(summary = "login")
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    void login(@RequestBody LogInRequest logInRequest) {
    }

}
