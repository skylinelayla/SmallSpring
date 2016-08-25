package xmlparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.w3c.dom.*;

import test.TestFactory;

import javax.xml.parsers.*;

public class BeanXMLParser {
	
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	
	private Document doc= null;
	
	private NodeList beanlist = null;
	
	public BeanXMLParser(String path){
		this.dbf = DocumentBuilderFactory.newInstance();
		try{
			this.db = dbf.newDocumentBuilder();
		
			this.doc = db.parse(new File(ClassLoader.getSystemResource("").toURI().getPath()+path));
		}catch(Exception e)
		{
			System.out.println("BeanXMLParser init failed");
		}
	}
	
	public void parse(){
		Element beansnode = (Element)doc.getElementsByTagName("beans").item(0);
		beanlist = beansnode.getElementsByTagName("bean");//获取所有的bean
//		System.out.println(beanlist.getLength());
		for (int i = 0 ; i< beanlist.getLength(); i++)
		{
			Element beanElement = (Element)beanlist.item(i);//bean节点
			String objid = beanElement.getAttributes().getNamedItem("id").getNodeValue();
			
			
			if(TestFactory.objectMap.get(objid)==null)
				this.rejectOneClass(beanElement);
			
			

		}
	}
	
	private Element getElementByBeanID(String beanid){
		for (int i = 0 ; i< beanlist.getLength(); i++)
		{
			Element beanElement = (Element)beanlist.item(i);//bean节点
			String objid = beanElement.getAttributes().getNamedItem("id").getNodeValue();
			
			
			if(objid.equals(beanid))
			{
				return beanElement;
			}
		}
		return null;
	}
	
	public void rejectOneClass(Element beanElement){
		
		String objid = beanElement.getAttributes().getNamedItem("id").getNodeValue();
		String objpath = beanElement.getAttributes().getNamedItem("class").getNodeValue();
		
		
//		System.out.println(objpath);
		Class<?> objclass = null;
		Object obj = null;
		try {
			objclass = Class.forName(objpath);
			obj = objclass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		NodeList propertylist= beanElement.getElementsByTagName("property");//属性方法
//		System.out.println(propertylist.getLength());
		for(int j = 0; j<propertylist.getLength();j++)//遍历属性和方法tag
		{
			Element pn = (Element)propertylist.item(j);
//			System.out.println(pn.getNodeName());
			NamedNodeMap nodemap = pn.getAttributes();
			String fieldname = nodemap.getNamedItem("name").getNodeValue();
			Field field = null;
			try {
				field = objclass.getDeclaredField(fieldname);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			String _fieldname = fieldname.substring(0, 1).toUpperCase()+fieldname.substring(1);
			
			if(nodemap.getNamedItem("value")==null&&nodemap.getNamedItem("ref")==null)//property没有声明值
			{
				//((ParameterizedType)field.getType().getGenericSuperclass());
				
				//System.out.println(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0].toString());
				if(field.getType().getName().equals(Map.class.getName()))//map类型
				{
					
					Method method = null;
					try {
						method = objclass.getMethod("set"+_fieldname, Map.class);
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Element mapnode = (Element)pn.getElementsByTagName("map").item(0);
					HashMap<String, Object> map = new HashMap<String, Object>();
					NodeList entrylist = mapnode.getElementsByTagName("entry");
					for(int m = 0; m < entrylist.getLength();m++)
					{
						Node n = (Node)entrylist.item(m);
						
						//System.out.println(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[1].toString().split(" ")[1]);
						if(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[1].toString().split(" ")[1].equals("java.lang.String"))
						{
							//System.out.println(n.getAttributes().getNamedItem("key").getNodeValue());
							map.put(n.getAttributes().getNamedItem("key").getNodeValue(),((Element)n).getElementsByTagName("value").item(0).getTextContent());
						}
						else if(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[1].toString().split(" ")[1].equals("java.lang.Integer"))
						{
							//System.out.println(((Element)n).getElementsByTagName("value").item(0).getTextContent());
							map.put(n.getAttributes().getNamedItem("key").getNodeValue(),Integer.parseInt(((Element)n).getElementsByTagName("value").item(0).getTextContent()));
						}
					}
					try {
						method.invoke(obj, map);
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
			else if(nodemap.getNamedItem("ref")!=null){
				Element element = this.getElementByBeanID(nodemap.getNamedItem("ref").getNodeValue());
				String beanid = element.getAttributes().getNamedItem("id").getNodeValue();
				
				Method method = null;
				if(TestFactory.objectMap.get(beanid)==null)
				{
					this.rejectOneClass(element);
					//System.out.println("test");
				}
				try {
					method = objclass.getMethod("set"+_fieldname, TestFactory.objectMap.get(beanid).getClass());
					method.invoke(obj, TestFactory.objectMap.get(beanid));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
//		System.out.println(objid);
		TestFactory.objectMap.put(objid, obj);
	}
	
	public void getJarClassNameAndMethod(String jarFile){
		File f = new File(jarFile);
		
		URL url1 = null;
		try {
			 url1 = f.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		URLClassLoader myClassLoader = new URLClassLoader(new URL[]{url1},Thread.currentThread().getContextClassLoader());
		JarFile jar = null;
		try {
			jar = new JarFile(jarFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Enumeration<JarEntry> enumFiles = jar.entries();
		JarEntry entry = null;
		while(enumFiles.hasMoreElements()){
			entry = (JarEntry)enumFiles.nextElement();
			if(entry.getName().indexOf("META-INF")<0){
				String classFullName = entry.getName();
				System.out.println(classFullName);
			}
		}
		
	}
	
}
