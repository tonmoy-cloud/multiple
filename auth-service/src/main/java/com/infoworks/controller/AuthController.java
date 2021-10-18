package com.infoworks.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@GetMapping("/validateToken")
	public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
		if (token == null || token.isEmpty())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized Access");
		return ResponseEntity.ok("Hello JavaInUse Called in Auth Service: get::validateToken");
	}

	@PostMapping("/validateToken")
	public ResponseEntity<String> validateTokenV2(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
		if (token == null || token.isEmpty())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized Access");
		return ResponseEntity.ok("Hello JavaInUse Called in Auth Service: post::validateToken");
	}
}
