package com.tongji.controller;

import java.util.Map;

import com.tongji.service.PrintService;

public class PrintController {
	private PrintService printService;
	
	public Map<String,Integer> testMap;

	public Map<String, Integer> getTestMap() {
		return testMap;
	}

	public void setTestMap(Map<String, Integer> testMap) {
		this.testMap = testMap;
	}
	
	public void print(String s){
		System.out.println(testMap.get(s));
		this.printService.print();
	}

	public PrintService getPrintService() {
		return printService;
	}

	public void setPrintService(PrintService printService) {
		this.printService = printService;
	}

}
