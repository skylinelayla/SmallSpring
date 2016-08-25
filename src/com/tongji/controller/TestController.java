package com.tongji.controller;

import com.tongji.service.TestService;

import annotation.Controller;
import annotation.Resource;

@Controller
public class TestController {
	@Resource("testService")
	private TestService testService;
	
	public void print(){
		this.testService.print();
	}

	public TestService getTestService() {
		return testService;
	}

	public void setTestService(TestService testService) {
		this.testService = testService;
	}
	
	
}
