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

public class AutoPaySetupInvoker {

	public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/webservices/CXAutoPaySetup";

	private CloseableHttpClient client;
	private Pattern pattern1, pattern2;
	
	private static AutoPaySetupInvoker instance = new AutoPaySetupInvoker();

	private  AutoPaySetupInvoker() {

		client = HttpClients.createDefault();

		pattern1 = Pattern.compile(
				"<ouaf:autoPayId>(\\d+)</ouaf:autoPayId>");
		pattern2 = Pattern.compile(
				"<ouaf:autoPayId/>");
	}
	
	public static AutoPaySetupInvoker getInstance()
	{
		return instance;
	}

	public boolean invoke(String acctid, String perid, boolean update) {
		HttpPost post = new HttpPost(WSDL);
		post.setEntity(new StringEntity(getPayload(acctid, perid, update), ContentType.TEXT_XML));
		post.setHeader("Content-type", "text/xml; charset=UTF-8");
		post.setHeader("Authorization", "Basic ZXNpZHVFJA==");

		try {
			// System.out.println(soapPayload);
			HttpResponse response = client.execute(post);

			return parse(preprocess(response));
		} catch (Exception e) {
			e.printStackTrace();
			//System.exit(500);
		}

		return false;
	}

	private String preprocess(HttpResponse response) {

		String text = null;
		try {
			text = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(text);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return text;
	}

	private boolean parse(String text) {
		PreferenceResults results = new PreferenceResults();

		Matcher matcher1 = pattern1.matcher(text);
		Matcher matcher2 = pattern2.matcher(text);

		if (matcher1.find()) {
			return true;
		} else if (matcher2.find()) {
			results.name = matcher2.group(1);
			return false;
		}

		return false;
	}

	private String getPayload(String acctid, String perid,boolean update)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wxb=\"http://ouaf.oracle.com/webservices/cx/CXAutoPaySetup\">");
		sb.append("	<soapenv:Header>");
		sb.append(
				"		<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">");
		sb.append(
				"			<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		sb.append("				<wsu:Created>").append(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
				.append("</wsu:Created>");
		sb.append("				<wsu:Expires>")
				.append(ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT))
				.append("</wsu:Expires>");
		sb.append("			</wsu:Timestamp>");
		sb.append("		</wsse:Security>");
		sb.append("	</soapenv:Header>");
		sb.append("	<soapenv:Body>");
		sb.append("		<wxb:CXAutoPaySetup>");
		sb.append("		<wxb:head>");
		sb.append("			<wxb:action>").append(update?"UPDATE":"READ").append("</wxb:action>");
		sb.append(" 			<wxb:key1>");
		sb.append("				<wxb:name>ACCT_ID</wxb:name>");
		sb.append("				<wxb:value>").append(acctid).append("</wxb:value>");
		sb.append("			</wxb:key1>");
		sb.append(" 			<wxb:key2>");
		sb.append("				<wxb:name>PER_ID</wxb:name>");
		sb.append("				<wxb:value>").append(perid).append("</wxb:value>");
		sb.append("			</wxb:key2>");
		sb.append("		</wxb:head>");
		
		if(update) {
			sb.append("		<wxb:mainData>");
			sb.append("		<wxb:autoPayInfo>");
			sb.append("					<wxb:paymentType>27</wxb:paymentType>");
			sb.append("					<wxb:bankRoutingNumber>789456124</wxb:bankRoutingNumber>");
			sb.append("					<wxb:externalAccountId>*2455</wxb:externalAccountId>");
			sb.append("					<wxb:name>Tiny Town</wxb:name>");
			sb.append("					<wxb:maxWithdrawAmount>0.00</wxb:maxWithdrawAmount>");
			sb.append("		</wxb:autoPayInfo>");
			sb.append("		</wxb:mainData>");
			
		}
		sb.append("	</wxb:CXAutoPaySetup>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		
		String payload= sb.toString();
		System.out.println();
		System.out.println("request\n\n\n");
		System.out.println(payload);
		return payload; 
	}
	public static void main(String args[]) {
		
		
		 AutoPaySetupInvoker loader = new AutoPaySetupInvoker();
		 boolean results;
		 


		// Gina
		//results = loader.invoke("9940491655", "3682185931", true);

		// Peggy
		//results = loader.invoke("6917171749","9051320808",true);
		
		// john

		results = loader.invoke("0794337011","4119807474",false);

		System.out.println(results?"Auto pay is already setup":"Autopay is not setup");
		System.out.println("Done. Exiting...");

	}

}
