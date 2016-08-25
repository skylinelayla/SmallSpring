package annotationparser;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import test.TestFactory;

import annotation.Controller;
import annotation.Resource;
import annotation.Service;

public class BeanAnnotationParser {
	public String basepkgname = null; 
	public File file = null;
	
	public BeanAnnotationParser(String pkgname){
		this.basepkgname = pkgname;
	}
	
	private Class<?> getClassByAnnotationName(File f, String annotationname){
		
		if(f.isDirectory()){
			File [] ffs = f.listFiles();
			for(File ff: ffs)
			{
				 Class<?> c = getClassByAnnotationName(ff,annotationname);
				 if(c != null)
					 return c;
			}
			
			
		}
		if(f.isFile()){
			
			String classname = f.getName().split("[.]")[0];
			String[] paths = f.getPath().split("[/]");
			int index = 0;
			StringBuffer packagename_temp = new StringBuffer("");
			for(String s: paths)
			{
				index++;
				if(s.equals("bin"))
				{
					break;
				}
				
			}
			for(int i = index;i<paths.length-1;i++){
				packagename_temp.append(paths[i]+".");
			}
			String pkgname = packagename_temp.substring(0, packagename_temp.length()-1);
			
//			System.out.println(pkgname+"."+classname);
			
			try {
				Class<?> classDef = Class.forName(pkgname+"."+classname);
				
				if(classDef.isAnnotationPresent(Service.class)){
					if(((Service)classDef.getAnnotation(Service.class)).value().equals(annotationname)){
						return classDef;
					}
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return null;
	}
	
	public void rejectOneClass(Class<?> classDef){
		Field [] fields = classDef.getDeclaredFields();
		Method[] methods = classDef.getMethods();
		
		Object classobject = null;
		String pkgname = classDef.getPackage().toString().split(" ")[1];
//		System.out.println(pkgname);
		
		
		try {
			classobject = classDef.newInstance();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Field field : fields){
			if(field.isAnnotationPresent(Resource.class)){
				String annotionname = ((Resource)field.getAnnotation(Resource.class)).value();
				Object o = null;
				
				if(TestFactory.objectMap.containsKey(annotionname)){
					o = TestFactory.objectMap.get(pkgname+"."+annotionname);
				}
				else
				{
//					System.out.println(annotionname);
					Class<?> classNeeded = getClassByAnnotationName(file,annotionname);
					rejectOneClass(classNeeded);
					
					o = TestFactory.objectMap.get(annotionname);
				}
				for(Method m: methods){
					if(m.getName().startsWith("set")){
						try {
							m.invoke(classobject,o);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		
		if(classDef.isAnnotationPresent(Service.class))
		{
			Service service = (Service)classDef.getAnnotation(Service.class);
			TestFactory.objectMap.put(service.value(),classobject);
//			System.out.println(pkgname+"."+service.value());
		}
		else if(classDef.isAnnotationPresent(Controller.class))
		{
			String [] classnames =classDef.getName().split("[.]");
			String classname = classnames[classnames.length-1];
			TestFactory.objectMap.put(pkgname+"."+classname,classobject);
//			System.out.println(pkgname+"."+classname);
		}
		
	}
	
	
	public void retrieveFiles(File f){
		
		
		if(f.isDirectory())
		{
			File[] files = f.listFiles();
			for(File ff: files)
			{
				retrieveFiles(ff);
			}
		}
		if(f.isFile())
		{
			String classname = f.getName().split("[.]")[0];
			String[] paths = f.getPath().split("[/]");
			int index = 0;
			StringBuffer packagename_temp = new StringBuffer("");
			for(String s: paths)
			{
				index++;
				if(s.equals("bin"))
				{
					break;
				}
				
			}
			for(int i = index;i<paths.length-1;i++){
				packagename_temp.append(paths[i]+".");
			}
			String pkgname = packagename_temp.substring(0, packagename_temp.length()-1);
			
			
//			System.out.println(pkgname);
			//反射生成对象
			try {
				Class<?> classDef = Class.forName(pkgname+"."+classname);
				
				if(classDef.isAnnotationPresent(Controller.class))
					rejectOneClass(classDef);
				else if(classDef.isAnnotationPresent(Service.class)){
					Service service = (Service)classDef.getAnnotation(Service.class);
					if(!TestFactory.objectMap.containsKey(pkgname+"."+service.value())){
						rejectOneClass(classDef);
					}
				}
				
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void parse(){
		//获取需要注入文件夹下的class文件名
//		File file = null;
		String basePath ="";
		
		try {
			String [] packages = basepkgname.split("[.]");
			StringBuffer packagepath = new StringBuffer("");
			for(String s : packages)
			{
				packagepath.append(s+"/");
			}
			basePath = ClassLoader.getSystemResource("").toURI().getPath()+packagepath;
//			System.out.println(basePath);
			file = new File(basePath);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		retrieveFiles(file);
	}
}
