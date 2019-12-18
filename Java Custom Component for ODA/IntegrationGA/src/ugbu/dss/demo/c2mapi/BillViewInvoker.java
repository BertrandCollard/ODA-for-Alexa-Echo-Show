package ugbu.dss.demo.c2mapi;

import java.time.LocalDate;
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




public class BillViewInvoker {
	
	public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/webservices/CXBillView";

	private CloseableHttpClient client;
	private String soapPayload;
	private Pattern pattern; 
	
	public BillViewInvoker(String acctid, String perid)
	{	
		 
		  client = HttpClients.createDefault();	 

		  
		  StringBuilder sb = new StringBuilder();
		  sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wxb=\"http://ouaf.oracle.com/webservices/cx/CXBillViews\">");
		  sb.append("	<soapenv:Header>");
		  sb.append("		<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"); 
		  sb.append("			<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		  sb.append("				<wsu:Created>").append(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Created>");
		  sb.append("				<wsu:Expires>").append(ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Expires>"); 
		  sb.append("			</wsu:Timestamp>");
		  sb.append("		</wsse:Security>"); 
		  sb.append("	</soapenv:Header>");
		  sb.append("	<soapenv:Body>");
		  sb.append("		<wxb:CXBillView>");
		  sb.append("		<wxb:head>");	
		  sb.append("			<wxb:action>READ</wxb:action>");
		  sb.append(" 			<wxb:key1>");
		  sb.append("				<wxb:name>ACCT_ID</wxb:name>");
		  sb.append("				<wxb:value>").append(acctid).append("</wxb:value>");
		  sb.append("			</wxb:key1>");
		  sb.append(" 			<wxb:key2>");
		  sb.append("				<wxb:name>PER_ID</wxb:name>");
		  sb.append("				<wxb:value>").append(perid).append("</wxb:value>");
		  sb.append("			</wxb:key2>");
		  sb.append("		</wxb:head>");
		  sb.append("	</wxb:CXBillView>");
		  sb.append("</soapenv:Body>");
		  sb.append("</soapenv:Envelope>");
		  
		  soapPayload = sb.toString();
		  
		  //pattern = Pattern.compile("<ouaf:endingBalance>(\\d+.\\d+)</ouaf:endingBalance>");
		  //pattern = Pattern.compile("<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>");
		 // pattern = Pattern.compile("<ouaf:endingBalance>(\\d+.\\d+)</ouaf:endingBalance>.*?<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>");
		  pattern = Pattern.compile("<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>.*?<ouaf:endingBalance>(\\d+.\\d+)</ouaf:endingBalance>");
	}
	
	
	public BillViewResults invoke()
	{		
		 HttpPost post = new HttpPost(WSDL);
		 post.setEntity(new StringEntity(soapPayload,ContentType.TEXT_XML));
		 post.setHeader("Content-type", "text/xml; charset=UTF-8");
		 post.setHeader("Authorization", "Basic SZXNpZHVFJA==");
		 

		try {
			System.out.println(soapPayload);
			HttpResponse response  = client.execute(post);
			
			return parse(response);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}
		
		return null;
	}
	
	private BillViewResults parse(HttpResponse response)
	{
		BillViewResults results = new BillViewResults();
		String text;
		try {
			text = EntityUtils.toString(response.getEntity(),"UTF-8");
			
			System.out.println("\n\n------"+text+"\n\n------");
			Matcher matcher = pattern.matcher(text);
			
			if(matcher.find()) {
				results.duedate = LocalDate.parse(matcher.group(1));;
				results.dueAmount = Float.valueOf(matcher.group(2));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return results;		
	}
	
	
	public static void main (String args[])
	{		
		BillViewInvoker loader = new BillViewInvoker("7881973462","6725703457");
		BillViewResults results = loader.invoke();
		
		results.print();
		System.out.println("Done. Exiting...");
		
	}
	
	
	
}
