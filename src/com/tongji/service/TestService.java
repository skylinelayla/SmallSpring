package com.tongji.service;

import annotation.Resource;
import annotation.Service;

@Service("testService")
public class TestService {
	
	@Resource("testService2")
	private TestService2 testService2;
	public void print(){
		//System.out.println("this is in test service");
		testService2.print();
	}
	public TestService2 getTestService2() {
		return testService2;
	}
	public void setTestService2(TestService2 testService2) {
		this.testService2 = testService2;
	}
}
