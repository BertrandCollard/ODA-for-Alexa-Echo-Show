package ugbu.dss.demo.c2mapi;

import java.security.KeyStore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class CustomerServiceStatus {

	public static String WSDL = "https://nc2m.oracleutilities.xyz:6201/ouaf/webservices/CMCustStatInq";

	//private CloseableHttpClient client;
        private DefaultHttpClient client;
	private Pattern base_pattern;
	private Pattern water_leak_pattern;
	private Pattern gas_leak_pattern;
	private Pattern restore_pattern;
	private Pattern NP_pattern;
	

	private static CustomerServiceStatus instance = new CustomerServiceStatus();

	private CustomerServiceStatus() {

		 try {
		        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		        trustStore.load(null, null);

		        NC2MSSLSocketFactory sf = new NC2MSSLSocketFactory(trustStore);
		        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		        HttpParams params = new BasicHttpParams();
		        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        registry.register(new Scheme("https", sf, 443));

		        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

		        client =  new DefaultHttpClient(ccm, params);
		    } catch (Exception e) {
		        client =  new DefaultHttpClient();
		    }
		 
	

		base_pattern = Pattern.compile("<ouaf:isOutage>(\\w+)</ouaf:isOutage>.+<ouaf:amtDue>(-?[0-9]+([,.][0-9]{1,2})?)</ouaf:amtDue>");
		water_leak_pattern = Pattern.compile("<ouaf:wtr><ouaf:lastActDttm>(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2})</ouaf:lastActDttm></ouaf:wtr>");
		gas_leak_pattern   = Pattern.compile("<ouaf:gas><ouaf:lastActDttm>(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2})</ouaf:lastActDttm></ouaf:gas>");
		NP_pattern = Pattern.compile("<ouaf:disconnectNPDate>(\\d{4}-\\d{2}-\\d{2})</ouaf:disconnectNPDate>");
		restore_pattern = Pattern.compile("<ouaf:estRestoreTime>(\\d+)</ouaf:estRestoreTime>");
	}

	public static CustomerServiceStatus getInstance() {
		return instance;
	}

	public CustomerServiceStatusResults invoke(String acctid, String perid) {
		HttpPost post = new HttpPost(WSDL);
		post.setEntity(new StringEntity(getPayload(acctid, perid), ContentType.TEXT_XML));
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

		return null;
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

	private CustomerServiceStatusResults parse(String text) {
		CustomerServiceStatusResults results = new CustomerServiceStatusResults();

		Matcher matcher = base_pattern.matcher(text);

		if (matcher.find()) {
			System.out.println(" outage/amt found");
			results.elec_outage = Boolean.parseBoolean(matcher.group(1));
			results.NP_amt =  Float.valueOf(matcher.group(2));
		}

		matcher = water_leak_pattern.matcher(text);
		if (matcher.find()) {
			System.out.println(" water leak  matcher found");
			results.water_leak_time = ZonedDateTime.parse(matcher.group(1));
		
		}
		
		matcher = gas_leak_pattern.matcher(text);
		if (matcher.find()) {
			System.out.println(" gas leak  matcher found");
			results.gas_leak_time = ZonedDateTime.parse(matcher.group(1));
		
		}
		
		if(results.NP_amt > 0)
		{
			matcher = NP_pattern.matcher(text);
			if (matcher.find()) {
				System.out.println(" NP matcher found");
				results.NP_date = LocalDate.parse(matcher.group(1));
			}
		}
		
		if(results.elec_outage)
		{
			matcher = restore_pattern.matcher(text);
			if (matcher.find()) {
				System.out.println(" restore matcher found");
				results.restore =matcher.group(1);
			}
		}	
				
		return results;
	}

	private String getPayload(String acctid, String perid) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wxb=\"http://ouaf.oracle.com/webservices/cm/CMCustStInq\">");
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
		sb.append("		<wxb:READ>");
		sb.append("			<wxb:acctId>").append(acctid).append("</wxb:acctId>");
		sb.append("			<wxb:personId>").append(perid).append("</wxb:personId>");
		sb.append("	</wxb:READ>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");

		String payload = sb.toString();
		System.out.println();
		System.out.println("request\n\n\n");
		System.out.println(payload);
		return payload;
	}

	public static void main(String args[]) {

		CustomerServiceStatus loader = CustomerServiceStatus.getInstance();
		CustomerServiceStatusResults results;

		// collection account
		//results = loader.invoke("2035172533", "");
		
		// outage account
		//results = loader.invoke("8406321309", "");
		results = loader.invoke("4518548137", "");

		results.print();
		System.out.println("Done. Exiting...");

	}



}
