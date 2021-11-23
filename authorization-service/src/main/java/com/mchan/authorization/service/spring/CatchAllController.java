package com.mchan.authorization.service.spring;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A Controller that will catch all the Endpoints and just return the Index Website.
 * This will be later used for building the SPA.
 */
@Controller
public class CatchAllController implements ErrorController {

    /**
     * .
     *
     * @return .
     */
    @RequestMapping(value = {
        "/",
        "/login/**",
        "/error"
    })
    public String index() {
        return "index.html";
    }

}
