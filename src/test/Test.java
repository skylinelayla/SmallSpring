package test;

import java.util.Map;

public class Test {
	public Map<String,Integer> testMap;

	public Map<String, Integer> getTestMap() {
		return testMap;
	}

	public void setTestMap(Map<String, Integer> testMap) {
		this.testMap = testMap;
	}
	
	public void print(String s){
		System.out.println(testMap.get(s));
	}
}
