package com.vmware.itda.chs.zapigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1")
public class HelloController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@GetMapping(value = "/hello")
	@ApiOperation(value = "Test if the service is running")
	public String getHello() {
		logger.info("Saying hello");
		return "Hello, Service User!";
	}
	
}
