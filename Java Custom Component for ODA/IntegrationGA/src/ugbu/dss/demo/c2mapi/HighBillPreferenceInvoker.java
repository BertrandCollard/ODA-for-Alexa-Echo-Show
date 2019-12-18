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
	
	public class HighBillPreferenceInvoker {
	
		public static String WSDL = "https://sphinx.oracleutilities.xyz:6201/ouaf/webservices/CXMaintainCommPreferences";
	
		private CloseableHttpClient client;
		private Pattern pattern1, pattern2;
		
		private static HighBillPreferenceInvoker instance = new HighBillPreferenceInvoker();
	
		private HighBillPreferenceInvoker() {
	
			client = HttpClients.createDefault();
	
			pattern1 = Pattern.compile(
					"(HIGH-CONSUMPTION).*?<ouaf:contactId>(\\d+)</ouaf:contactId><ouaf:contactValue>([\\w.@]+?)</ouaf:contactValue><ouaf:nickname/><ouaf:ntfPreferenceId>(\\d+)</ouaf:ntfPreferenceId>");
			pattern2 = Pattern.compile(
					"(HIGH-CONSUMPTION).*?<ouaf:contactId>(\\d+)</ouaf:contactId><ouaf:contactValue>([\\w.@]+?)</ouaf:contactValue><ouaf:nickname/><ouaf:ntfPreferenceId/>");
		}
	
		public static HighBillPreferenceInvoker getInstance()
		{
			return instance;
		}
		
		public PreferenceResults invoke(String acctid, String perid, PreferenceResults results) {
			HttpPost post = new HttpPost(WSDL);
			post.setEntity(new StringEntity(getPayload(acctid, perid, results), ContentType.TEXT_XML));
			post.setHeader("Content-type", "text/xml; charset=UTF-8");
			post.setHeader("Authorization", "Basic XNpZHVFJA==");
	
			try {
				// System.out.println(soapPayload);
				HttpResponse response = client.execute(post);
	
				return parse(preprocess(response));
			} catch (Exception e) {
				e.printStackTrace();
				//System.exit(500);
			}
	
			return null;
		}
	
		private String preprocess(HttpResponse response) {
	
			String text = null;
			try {
				text = EntityUtils.toString(response.getEntity(), "UTF-8");
	
				int start = text.indexOf("HIGH-CONSUMPTION");
				int end = text.indexOf("</ouaf:subscriptionNotificationTypes>", start);
				System.out.println(text);
	
				text = text.substring(start, end);
				System.out.println("start: "+start+" end: "+end);
				System.out.println(text);
	
			} catch (Exception e) {
				e.printStackTrace();
	
			}
			return text;
		}
	
		private PreferenceResults parse(String text) {
			PreferenceResults results = new PreferenceResults();
	
			Matcher matcher1 = pattern1.matcher(text);
			Matcher matcher2 = pattern2.matcher(text);
	
			if (matcher1.find()) {
				results.name = matcher1.group(1);
				results.contactid = matcher1.group(2);
				results.contact = matcher1.group(3);
				results.xref = matcher1.group(4);
				results.signedup = true;
			} else if (matcher2.find()) {
				results.name = matcher2.group(1);
				results.contactid = matcher2.group(2);
				results.contact = matcher2.group(3);
				results.signedup = false;
			}
	
			return results;
		}
	
		private String getPayload(String acctid, String perid,PreferenceResults result)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wxb=\"http://ouaf.oracle.com/webservices/cx/CXMaintainCommPreferences\">");
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
			sb.append("		<wxb:CXMaintainCommPreferences>");
			sb.append("		<wxb:head>");
			sb.append("			<wxb:action>").append(result != null?"UPDATE":"READ").append("</wxb:action>");
			sb.append(" 			<wxb:key1>");
			sb.append("				<wxb:name>ACCT_ID</wxb:name>");
			sb.append("				<wxb:value>").append(acctid).append("</wxb:value>");
			sb.append("			</wxb:key1>");
			sb.append(" 			<wxb:key2>");
			sb.append("				<wxb:name>PER_ID</wxb:name>");
			sb.append("				<wxb:value>").append(perid).append("</wxb:value>");
			sb.append("			</wxb:key2>");
			sb.append("		</wxb:head>");
			
			if(result != null) {
				sb.append("		<wxb:mainData>");
				sb.append("		<wxb:subscriptionNotificationTypes>");
				sb.append("			<wxb:notificationType>HIGH-CONSUMPTION</wxb:notificationType>");
				sb.append("				<wxb:deliveryTypes>");
				sb.append("					<wxb:deliveryType>EMAI</wxb:deliveryType>");
				sb.append("					<wxb:deliveryTypeDescription>Email</wxb:deliveryTypeDescription>");
				sb.append("					<wxb:personContacts>");
				sb.append("							<wxb:contactId>").append(result.contactid).append("</wxb:contactId>");
				sb.append("							<wxb:contactValue>").append(result.contact).append("</wxb:contactValue>");
				sb.append("							<wxb:updateComPref>true</wxb:updateComPref>");
				sb.append("					</wxb:personContacts>");
				sb.append("				</wxb:deliveryTypes>");
				sb.append("		</wxb:subscriptionNotificationTypes>");
				sb.append("		</wxb:mainData>");
				
			}
			sb.append("	</wxb:CXMaintainCommPreferences>");
			sb.append("</soapenv:Body>");
			sb.append("</soapenv:Envelope>");
			
			String payload= sb.toString();
			System.out.println();
			System.out.println("request\n\n\n");
			System.out.println(payload);
			return payload; 
		}
		public static void main(String args[]) {
			
			
			 HighBillPreferenceInvoker loader =  HighBillPreferenceInvoker.getInstance();
			 PreferenceResults results;
			 
			// william
			// HighBillPreferenceInvoker loader = new
			// HighBillPreferenceInvoker("7881973462","6725703457");
	
			// Gina
			//results = loader.invoke("9940491655", "3682185931", true);
	
			// Peggy
			results = loader.invoke("6917171749","9051320808",null);
			results = loader.invoke("6917171749","9051320808",results);
	
			results.print();
			System.out.println("Done. Exiting...");
	
		}
	
	}
