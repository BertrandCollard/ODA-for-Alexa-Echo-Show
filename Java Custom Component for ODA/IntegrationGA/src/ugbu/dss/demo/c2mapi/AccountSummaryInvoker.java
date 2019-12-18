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




public class AccountSummaryInvoker {
	
	public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/webservices/CXAccountChargesSummaryRetriever";

	private CloseableHttpClient client;
	private Pattern pattern; 
	
	private static AccountSummaryInvoker instance = new AccountSummaryInvoker();
	
	private AccountSummaryInvoker()
	{	
		 
		  client = HttpClients.createDefault();	 
		  
		  //pattern = Pattern.compile("<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>.*?<ouaf:endingBalance>(\\d+.\\d+)</ouaf:endingBalance>");
		  //pattern = Pattern.compile("<ouaf:amount>([0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amount>([0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amount>([0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amountDue>([0-9]+([,.][0-9]{1,2})?)</ouaf:amountDue>.*?<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>");
		  
		  // c2M 2.6 
		  // pattern = Pattern.compile("<ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:amountDue>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amountDue>.*?<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>");
		  
		  // C2M 2.7 amounts
		  pattern = Pattern.compile("<ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+<ouaf:label>Electric</ouaf:label><ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>.+?(<ouaf:amount>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amount>)?.+?<ouaf:amountDue>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amountDue>.*?<ouaf:dueDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:dueDate>");

	}
	

	private String getPayload(String acctid, String perid)
	{
	
	  StringBuilder sb = new StringBuilder();
	  sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wxb=\"http://ouaf.oracle.com/webservices/cx/CXAccountChargesSummaryRetriever\">");
	  sb.append("	<soapenv:Header>");
	  sb.append("		<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"); 
	  sb.append("			<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
	  sb.append("				<wsu:Created>").append(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Created>");
	  sb.append("				<wsu:Expires>").append(ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT)).append("</wsu:Expires>"); 
	  sb.append("			</wsu:Timestamp>");
	  sb.append("		</wsse:Security>"); 
	  sb.append("	</soapenv:Header>");
	  sb.append("	<soapenv:Body>");
	  sb.append("		<wxb:CXAccountChargesSummaryRetriever>");
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
	  sb.append("	</wxb:CXAccountChargesSummaryRetriever>");
	  sb.append("</soapenv:Body>");
	  sb.append("</soapenv:Envelope>");
	  
	  System.out.println(sb);
	  return   sb.toString();
	}
	
	public static AccountSummaryInvoker getInstance()
	{
		return instance;
	}
	  
	public AccountSummaryResults invoke(String acctid, String perid)
	{		
		 HttpPost post = new HttpPost(WSDL);
		 post.setEntity(new StringEntity(getPayload(acctid, perid),ContentType.TEXT_XML));
		 post.setHeader("Content-type", "text/xml; charset=UTF-8");
		 post.setHeader("Authorization", "Basic SZXNpZHVFJA==");
		 

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
		
		return null;
	}
	
	private AccountSummaryResults parse(HttpResponse response)
	{
		AccountSummaryResults results = new AccountSummaryResults();
		String text;
		
	
		try {
			text = EntityUtils.toString(response.getEntity(),"UTF-8");
			System.out.println(text);
			Matcher matcher = pattern.matcher(text);
			
			if(matcher.find()) {
				/* c2m 2.6
				results.prev = Float.valueOf(matcher.group(1));
				results.cur = Float.valueOf(matcher.group(3));
				results.payments = Float.valueOf(matcher.group(5));
				results.dueAmount = Float.valueOf(matcher.group(7));
				results.duedate = LocalDate.parse(matcher.group(9));;
				*/
				// C2M 2.7
				results.prev = Float.valueOf(matcher.group(1));
				results.cur = Float.valueOf(matcher.group(3));
				results.dueAmount = Float.valueOf(matcher.group(8));
				results.duedate = LocalDate.parse(matcher.group(10));
				results.payments = results.cur + results.prev - results.dueAmount;
			}
			
			else
				System.out.println("******\n\n Not able to parse \n\n******\n\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return results;		
	}
	
	
	public static void main (String args[])
	{	
		// william
		//BillViewInvoker loader = new BillViewInvoker("7881973462","6725703457");
		
		//Gina
		AccountSummaryInvoker loader = new AccountSummaryInvoker();
		AccountSummaryResults results = loader.invoke("9940491655","3682185931");
		
		results.print();
		System.out.println("Done. Exiting...");
		
	}
	
	
	
}
