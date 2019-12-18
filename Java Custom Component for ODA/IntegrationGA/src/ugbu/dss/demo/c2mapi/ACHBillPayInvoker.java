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




public class ACHBillPayInvoker {
	
	public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/webservices/CXMakePayment";

	public static String ROUTING="789456124";
	public static String ACCT_NO="*****366";
	private static String PAYMENT_TYPE="27";
			
	private CloseableHttpClient client;
	private Pattern pattern; 
	private static ACHBillPayInvoker instance = new ACHBillPayInvoker();
	
	
	
	private ACHBillPayInvoker()
	{		 
		  client = HttpClients.createDefault();	 	 
		  pattern = Pattern.compile("<ouaf:referenceId>(\\d+)</ouaf:referenceId>");
	}
	
	public static ACHBillPayInvoker getInstance()
	{
		return instance;
	}
	
	public String invoke(String acctid, String perid,double amt)
	{		
		 HttpPost post = new HttpPost(WSDL);
		 String payload = getPayload( acctid,  perid, amt);
		 post.setEntity(new StringEntity(payload,ContentType.TEXT_XML));
		 post.setHeader("Content-type", "text/xml; charset=UTF-8");
		 post.setHeader("Authorization", "Basic NpZHVFJA==");
		 

		try {
			System.out.println(payload);
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
	
	private String getPayload(String acctid, String perid,double amt)
	{
	  StringBuilder sb = new StringBuilder();
	  sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns2=\"http://ouaf.oracle.com/webservices/cx/CXMakePayment\">");
	  sb.append("	<soapenv:Header>");
	  sb.append("		<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"); 
	  sb.append("			<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
	  sb.append("				<wsu:Created>").append(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Created>");
	  sb.append("				<wsu:Expires>").append(ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Expires>"); 
	  sb.append("			</wsu:Timestamp>");
	  sb.append("		</wsse:Security>"); 
	  sb.append("	</soapenv:Header>");
	  sb.append("	<soapenv:Body>");
	  sb.append("		<ns2:CXMakePayment>");
	  sb.append("		<ns2:head>");	
	  sb.append("			<ns2:action>ADD</ns2:action>");
	  sb.append(" 			<ns2:key1>");
	  sb.append("				<ns2:name>ACCT_ID</ns2:name>");
	  sb.append("				<ns2:value>").append(acctid).append("</ns2:value>");
	  sb.append("			</ns2:key1>");
	  sb.append(" 			<ns2:key2>");
	  sb.append("				<ns2:name>PER_ID</ns2:name>");
	  sb.append("				<ns2:value>").append(perid).append("</ns2:value>");
	  sb.append("			</ns2:key2>");
	  sb.append("			</ns2:head>");
	  sb.append("			<ns2:mainData>");
	  sb.append("				<ns2:paymentAmount>").append(String.valueOf(amt)).append("</ns2:paymentAmount>");
	  sb.append("				<ns2:paymentDate>").append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</ns2:paymentDate>");
	  sb.append("				<ns2:paymentType>").append(PAYMENT_TYPE).append("</ns2:paymentType>");
	  sb.append("					<ns2:bankAccountInfo>");
	  sb.append("						<ns2:routingNumber>").append(ROUTING).append("</ns2:routingNumber>");
	  sb.append("						<ns2:accountNumber>").append(ACCT_NO).append("</ns2:accountNumber>");
	  sb.append("					</ns2:bankAccountInfo>");
	  sb.append("					<ns2:saveCurrentPaymentDetails>false</ns2:saveCurrentPaymentDetails>");
	  sb.append("			</ns2:mainData>");
	  sb.append("	</ns2:CXMakePayment>");
	  sb.append("</soapenv:Body>");
	  sb.append("</soapenv:Envelope>");
	  
	 return sb.toString();
}
	  
	private String parse(HttpResponse response)
	{
		String text;
		try {
			text = EntityUtils.toString(response.getEntity(),"UTF-8");
			
			System.out.println(text);
			Matcher matcher = pattern.matcher(text);
			
			if(matcher.find()) {
				return  matcher.group(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return null;		
	}
	
	
	public static void main (String args[])
	{		
		ACHBillPayInvoker loader = ACHBillPayInvoker.getInstance();
		String results = loader.invoke("6917171749","9051320808",10.05f);
		
		System.out.println("reference id "+results);
		System.out.println("Done. Exiting...");
		
	}
	
	
	
}
