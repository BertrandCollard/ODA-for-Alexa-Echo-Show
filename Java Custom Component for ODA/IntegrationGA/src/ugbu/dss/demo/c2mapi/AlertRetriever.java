package ugbu.dss.demo.c2mapi;


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




public class AlertRetriever {
	
	
	public static String BASE_URL = "https://damp-woodland-96911.herokuapp.com/actions?acct=";
	
	private CloseableHttpClient client;

	// private static final Logger LOGGER = LoggerFactory.getLogger(UtilcoGAWebHookApp.class);
	 
	 private static AlertRetriever instance = new AlertRetriever();
	
	private AlertRetriever()
	{	 
		client = HttpClients.createDefault();
	}
	
	
	public String getAlerts(String acct)
	{		
		 HttpGet get = new HttpGet(BASE_URL+acct);

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
	
	
	
	public static AlertRetriever getInstance()
	{
		return instance;
	}
	
	public static void main (String args[])
	{		
		AlertRetriever ar = AlertRetriever.getInstance();
		
		System.out.println("acct1:"+ar.getAlerts("acct1"));
		System.out.println("acct2:"+ar.getAlerts("acct2"));
		System.out.println("acct3:"+ar.getAlerts("acct3"));
		System.out.println("acct4:"+ar.getAlerts("acct4"));
		System.out.println("acct5:"+ar.getAlerts("acct5"));
		System.out.println("acct6:"+ar.getAlerts("acct6"));
		System.out.println("acct7:"+ar.getAlerts("acct7"));
	}
	
	
	
}
