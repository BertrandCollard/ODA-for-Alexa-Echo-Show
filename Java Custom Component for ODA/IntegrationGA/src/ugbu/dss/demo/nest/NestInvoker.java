package ugbu.dss.demo.nest;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ugbu.tinytown.ga.UtilcoGAWebHookApp;




public class NestInvoker {
	
	// tinytown
	
	public static String URL = "https://firebase-apiserver24-tah01-iad01.dapi.production.nest.com:9553/devices/thermostats/";
	//public static String BASE_URL = "https://developer-api.nest.com/devices/thermostats/";	
	public static String TSTAT= "amNJgixgU9_qKEMTSdv_zxrK-kH3YiHw";
	public  String url = URL+TSTAT;
	
	public static String AUTHORIZATION = "Bearer c.zjJ9ioIgHjJHqrk7CxeoEkmOOn4FHDnrFaf72Thann81itk2svFp9hXJlronMBr2ranfzEVSKcE1MJN79cJlOdAKVhgHfJ1AbDzsgewPXC7jd1xoc8w3RgYFmdJhL35qGuQVRn3qQ4RP1jvj";
	
	
	
	/*
	//testbeds UoW
	   public static String URL = "https://firebase-apiserver43-tah01-iad01.dapi.production.nest.com:9553/devices/thermostats/";	
	   public static String TSTAT= "mMTe0BzH3cqPIJpFwUznvRrK-kH3YiHw";
	 public  String url = URL+TSTAT;
	
	 public static String AUTHORIZATION = "Bearer c.wO26qjj24ewHVek01s79REAYqIvOoaq7g6a8ufWb4OF37vdDCFqx8xkMwq7b2fsv0VSDbJIqFXwDOAryQLnBi38PhZXZnbYwlsMT6VxcUJ1cVeGEJZxmcqsOR0h4X4R85Sqzrv3HiqGodeRC";

	// wally
	//public static String AUTHORIZATION = "Bearer c.5dFjru5v0Tpa5C8lsqdUsIDeRJr9DxY9d1Yg0IB1ljC2tT0XZfRmwD2jdo9YdZoBryFhE0MuxR2fOtNcDbR3UAxjcO540xPQX74gWuKJSxgUNaO8YioErvs2Rgllf4oEYFqZl2ZqMsAGzM9R";
	*/
	private CloseableHttpClient client;

	 private static final Logger LOGGER = LoggerFactory.getLogger(UtilcoGAWebHookApp.class);
	 
	 private static NestInvoker instance = new NestInvoker();
	
	private NestInvoker()
	{	 
		client = HttpClients.createDefault();
	}
	
	
	public String readTargetTemprature()
	{		
		 HttpGet get = new HttpGet(url+"/target_temperature_f");
		 get.setHeader("Content-type", "application/json");
		 get.setHeader("Authorization", AUTHORIZATION);
		 

		try {
			HttpResponse response  = client.execute(get);
			
			return EntityUtils.toString(response.getEntity(),"UTF-8");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}
		
		return null;
	}
	
	public String setTargetTemprature(String temp)
	{
		return _setTargetTemprature(temp);
	}
	
	private String _setTargetTemprature(String temp)
	{		
		 String jsonPayload = "{\"target_temperature_f\":"+temp+"}";
		 
		 
		String response = null;
		try {

			
			 while(true)
			 {
				 LOGGER.info("pointing  to "+url);
				 HttpPut put = new HttpPut(url);
				 put.setEntity(new StringEntity(jsonPayload,ContentType.APPLICATION_JSON));
				 put.setHeader("Content-type", "application/json");
				 put.setHeader("Authorization", AUTHORIZATION);
	
				 
				 HttpResponse http_response  = client.execute(put);
	
				if(http_response.getStatusLine().getStatusCode()==307) {
					url = http_response.getFirstHeader("Location").getValue();
					LOGGER.info("Redirected to "+url);
				}
				else {
					response =  EntityUtils.toString(http_response.getEntity(),"UTF-8");
					break;
				}
			 }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}
		
		return response;
	}
	
	
	public static NestInvoker getInstance()
	{
		return instance;
	}
	
	public static void main (String args[])
	{		
		NestInvoker nest = NestInvoker.getInstance();
		
		String  temp = nest.readTargetTemprature();	
		System.out.println("temp: "+temp);
		
		String out = nest.setTargetTemprature("65");
		System.out.println("result: "+out);
		
		temp = nest.readTargetTemprature();	
		System.out.println("temp: "+temp);
		
		 out = nest.setTargetTemprature("63");
		System.out.println("result: "+out);
	}
	
	
	
}
