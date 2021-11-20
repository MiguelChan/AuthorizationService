package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.LogInRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * .
 */
@RestController
@RequestMapping("/")
@Api("Authentication")
public class LogInController {

    /**
     * .
     *
     * @param logInRequest .
     */
    @ApiOperation("login")
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    void login(@RequestBody LogInRequest logInRequest) {
    }

}
