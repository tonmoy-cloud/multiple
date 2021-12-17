package com.infoworks.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class SecondController {

    @GetMapping("/message")
    public String test(){
        return "Hello JavaInUse Called in Second Service";
    }

}
