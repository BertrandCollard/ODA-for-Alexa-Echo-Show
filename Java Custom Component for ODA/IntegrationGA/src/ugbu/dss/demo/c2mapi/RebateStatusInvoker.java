package ugbu.dss.demo.c2mapi;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;




public class RebateStatusInvoker {
	
	public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/XAIApp/xaiserver/CM_RetrieveOpenRebateClaim";

	private CloseableHttpClient client;
	private Pattern pattern; 
	
	private static RebateStatusInvoker instance = new RebateStatusInvoker();
	
	private RebateStatusInvoker()
	{	
		 
		  client = HttpClients.createDefault();	 
		  
		  pattern = Pattern.compile("<rowCount>(\\d+)</rowCount>");
	}
	

	private String getPayload(String acctid, String perid,String preid)
	{
	
	  StringBuilder sb = new StringBuilder();
	  sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cm=\"http://oracle.com/CM_RetrieveOpenRebateClaim.xsd\">");
	  sb.append("	<soapenv:Header>");
	  sb.append("		<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"); 
	  sb.append("			<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
	  sb.append("				<wsu:Created>").append(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Created>");
	  sb.append("				<wsu:Expires>").append(ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Expires>"); 
	  sb.append("			</wsu:Timestamp>");
	  sb.append("		</wsse:Security>"); 
	  sb.append("	</soapenv:Header>");
	  sb.append("	<soapenv:Body>");
	  sb.append("		<cm:CM_RetrieveOpenRebateClaim dateTimeTagFormat=\"xsd:strict\">");
	  sb.append("			<cm:personId>").append(perid).append("</cm:personId>");
	  sb.append("			<cm:accountId>").append(acctid).append("</cm:accountId>");
	  sb.append(" 			<cm:premiseId>").append(preid).append("</cm:premiseId>");
	  sb.append(" 			<cm:conservationProgram>APPLIANCES</cm:conservationProgram>");
	  sb.append("		 </cm:CM_RetrieveOpenRebateClaim>");
	  sb.append("</soapenv:Body>");
	  sb.append("</soapenv:Envelope>");
	  
	  return   sb.toString();
	}
	
	public static RebateStatusInvoker getInstance()
	{
		return instance;
	}
	  
	public int invoke(String acctid, String perid,String preid)
	{		
		 HttpPost post = new HttpPost(WSDL);
		 post.setEntity(new StringEntity(getPayload(acctid, perid,preid),ContentType.TEXT_XML));
		 post.setHeader("Content-type", "text/xml; charset=UTF-8");
		 post.setHeader("Authorization", "Basic XNpZHVFJA==");
		 

		try {
			//System.out.println(soapPayload);
			HttpResponse response  = client.execute(post);
			
			return parse(response);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}
		
		return 0;
	}
	
	private int parse(HttpResponse response)
	{
		
		String text;
		
	
		try {
			text = EntityUtils.toString(response.getEntity(),"UTF-8");
			System.out.println(text);
			Matcher matcher = pattern.matcher(text);
			
			if(matcher.find()) {
				return Integer.parseInt(matcher.group(1));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return 0;		
	}
	
	
	public static void main (String args[])
	{	
		// william
		//BillViewInvoker loader = new BillViewInvoker("7881973462","6725703457");
		
		//Gina
		//RebateSubmitInvoker loader = new RebateSubmitInvoker();
		//AccountSummaryResults results = loader.invoke("9940491655","3682185931");
		
		//Jon
		RebateStatusInvoker loader = new RebateStatusInvoker();
		//int claims = loader.invoke("0794337011","4119807474","6505546551");
		int claims = loader.invoke("0794337011","","");
		
		System.out.println(" # claims: "+claims);
		System.out.println("Done. Exiting...");
		
	}
	
	
	
}
