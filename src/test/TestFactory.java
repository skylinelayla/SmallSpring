package test;

import java.util.HashMap;

import xmlparser.BeanXMLParser;

import com.tongji.controller.PrintController;
import com.tongji.controller.TestController;

//import com.tongji.controller.TestController;
//import com.tongji.service.TestService;

import annotationparser.BeanAnnotationParser;

public class TestFactory {
	
	public static HashMap<String,Object> objectMap = new HashMap<String,Object>();
	
	
	public static void main(String [] args)
	{	
		
		BeanAnnotationParser BAP = new BeanAnnotationParser("com.tongji");
		BAP.parse();
		
		TestController tc= (TestController)objectMap.get("com.tongji.controller.TestController");
		
		tc.print();
		
		BeanXMLParser BMP = new BeanXMLParser("beans.xml");
		BMP.parse();
		
		//BMP.getJarClassNameAndMethod("/Users/panyan/Desktop/SmallSpring.jar");
		
		Test test = (Test)objectMap.get("test");
		test.print("b");
		
		PrintController pc = (PrintController)objectMap.get("printController");
		pc.print("a");
		
		com.testjar.entity.Test test3 = (com.testjar.entity.Test)objectMap.get("test3");
		test3.print("a");
	}
}
