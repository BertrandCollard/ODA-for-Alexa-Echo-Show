package ugbu.dss.demo.ofsc;


import java.text.Format;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;







public class OFSCScheduler {


	public static String BASE_URL = "https://api.etadirect.com/rest/";
	public static String URL_AVAIL_SCHEDULE = BASE_URL+"ofscCapacity/v1/activityBookingOptions/?activityType=FA&postalCode=32718&dates=";
	public static String URL_SCHEDULE_BOOK = BASE_URL+"ofscCore/v1/activities";
	public static String AUTHORIZATION = "Basic xxxxx";

	private CloseableHttpClient client;

	private static OFSCScheduler instance = new OFSCScheduler();

	private OFSCScheduler()
	{	 
		client = HttpClients.createDefault();
	}

	public String getAvailableDate()
	{
		LocalDate date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
		
		return date.format(DateTimeFormatter.ISO_DATE);
	}

	public List<String> getAvailableSchedules(String date)

	{
		
		System.out.println ("Getting schedules for: "+date);
		HttpGet get = new HttpGet(URL_AVAIL_SCHEDULE+date);
		get.setHeader("Content-type", "application/json");
		get.setHeader("Authorization", AUTHORIZATION);


		try {
			HttpResponse response  = client.execute(get);

			return parseDates(EntityUtils.toString(response.getEntity(),"UTF-8"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}

		return null;
	}
	
	public boolean bookAppointment(String date, String hslot)
	{
		String slot = from_human(hslot);
				
		System.out.println("slot ="+slot);
		String jsonPayload = getPayload(date,slot );

		
		try {
				HttpPost post = new HttpPost(URL_SCHEDULE_BOOK);
				post.setEntity(new StringEntity(jsonPayload,ContentType.APPLICATION_JSON));
				post.setHeader("Content-type", "application/json");
				post.setHeader("Authorization", AUTHORIZATION);


				HttpResponse response  = client.execute(post);
				
				System.out.println(EntityUtils.toString(response.getEntity(),"UTF-8"));

				return (response.getStatusLine().getStatusCode() == 201)?true:false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(500);
		}

		return false;
	}

		
	private String getPayload(String date, String slot)
	{
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"resourceId\": \"33028\",");
		sb.append("\"date\": \"").append(date).append("\",");
		sb.append("\"activityType\": \"FA\",");
		sb.append("\"timeSlot\": \"").append(slot).append("\",");
		sb.append("\"customerName\": \"John Doe\",");
		sb.append("\"streetAddress\": \"12345 Stuyvesant Ave\",");
		sb.append("\"city\": \"Brooklyn\",");
		sb.append("\"postalCode\": \"32718\"");
		sb.append(" }");
		     

		return sb.toString();
	}

	private List<String> parseDates(String string)
	{
		System.out.println(string);
	 
		List<String> dates = new ArrayList<String>();
		try {
			JSONObject response = (JSONObject) new JSONParser().parse(string);
			
			Map areas  = (Map)((JSONArray)((Map)((JSONArray)response.get("dates")).get(0)).get("areas")).get(0);
										
			JSONArray slots = (JSONArray)areas.get("timeSlots");
				
			Iterator itr = slots.iterator();
			
			while(itr.hasNext())
			{
				Map slot = (Map) itr.next();
				if(Integer.parseInt(slot.get("remainingQuota").toString()) >  0) {
					String name = to_human(slot.get("label").toString());
					if (!name.equalsIgnoreCase("all-day"))
						dates.add(name);
				}
				
			}
			
			
					
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return dates;
		
	}
	
	private String to_human(String slot)
	{
		String hours[] = slot.split("-");
		
		if(hours.length != 2)
			return slot;
		
		int start = Integer.parseInt(hours[0]);
		int stop = Integer.parseInt(hours[1]);
		
		if (start < 12  && stop < 12 )
			return ""+start+" to "+stop+" a.m.";
		
		if (start > 12  && stop > 12 )
			return ""+(start-12)+" to "+(stop-12) +" p.m.";
		
		if (stop == 12 )
			return ""+start+" to "+stop +" p.m.";
		
		if (start == 12 )
			return ""+start+" to "+(stop-12) +" p.m.";
		
		return slot;
		
	}

	private String from_human(String slot)
	{
		String seg[] = slot.split(" ");
		
		if(seg.length != 4)
			return slot;
		
		int start = Integer.parseInt(seg[0]);
		int stop = Integer.parseInt(seg[2]);
		
		if (seg[3].equals("a.m.") )
			return  String.format("%02d-%02d",start,stop);
		
		if (seg[3].equals("p.m.") && stop == 12 )
			return  String.format("%02d-%02d",start,stop);
		
		if (seg[3].equals("p.m.") && stop < 12 )
			return  String.format("%02d-%02d",start+12,stop+12);
					
		
		return slot;
		
	}
	public static OFSCScheduler getInstance()
	{
		return instance;
	}

	public static void main (String args[])
	{		
		
		
		
		OFSCScheduler ofsc = OFSCScheduler.getInstance();
		String date =  ofsc.getAvailableDate();	
		System.out.println(date);
		
		List<String> sch = ofsc.getAvailableSchedules(date);	
		System.out.println(sch);
		
		//boolean status = ofsc.bookAppointment(date, "3 to 5 pm");
		//System.out.println("Appointment booking successful: "+status);
		
		//sch = ofsc.getAvailableSchedules(1);		
		//System.out.println(sch);
	}



}
