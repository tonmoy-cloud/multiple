package com.infoworks.config;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/fallback/messages")
public class FallbackMessages {

    @RequestMapping(value = "/unreachable", method = GET, produces = "application/json")
    @ResponseBody
    public String fallBack(){
        return "Unfortunately api not reachable at this moment!";
    }

}
