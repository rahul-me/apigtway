package com.vmware.itda.chs.zapigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/probe")
public class ProbeController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping(value = "/ready")
	@ApiOperation(value = "Endpoint for checking shortly after startup whether service is ready to "
			+ "receive traffic.")
	public ResponseEntity<Object> ready() {
		logger.trace("Readiness check passed");
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	@GetMapping(value = "/live")
	public ResponseEntity<Object> live() {
		logger.trace("Liveness check passed");
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
}
