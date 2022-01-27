package com.infoworks.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class FirstController {

	@GetMapping("/message")
	public String test() {
		return "Hello JavaInUse Called in First Service";
	}

	@GetMapping("/delayed/message")
	public String testDelay() throws InterruptedException {
		Thread.sleep(5000);
		return "Hello JavaInUse Called First Service in 5Sec Delay";
	}

	@GetMapping("/error/{args}")
	public String errorMessage(@PathVariable("args") Integer args){
		return "Argument provided properly";
	}

	@GetMapping("/error/throw/{throw}")
	public String errorException(@PathVariable("throw") boolean shouldThrow){
		if (shouldThrow) throw new RuntimeException("Thrown from /error/...");
		return "No message has been thrown";
	}

	@GetMapping("/errorFallback")
	public String errorFallback() {
		return "Unfortunately api not reachable at this moment!";
	}

}
