package ugbu.dss.demo.c2mapi;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class CustomerServiceStatusResults {
	
	public static int LEAK_CLEAR_SECONDS = 90;
	
	public boolean elec_outage;
	public ZonedDateTime gas_leak_time;
	public ZonedDateTime water_leak_time;
	public float NP_amt;
	public LocalDate NP_date;
	public String restore;
	
	public CustomerServiceStatusResults()
	{}

	public boolean hasWaterLeak()
	{
		return hasLeak(water_leak_time);
	}
	
	public boolean hasGasLeak()
	{
		return hasLeak(gas_leak_time);
	}

	private boolean hasLeak(ZonedDateTime leak_time)
	{
		return (leak_time != null) ?leak_time.minusHours(3).isAfter( ZonedDateTime.now().minusSeconds(LEAK_CLEAR_SECONDS)):false;
	}
	public void print()
	{
		System.out.println("elec outage: "+elec_outage);
		System.out.println("gas leak: "+gas_leak_time + " has leak: "+hasGasLeak());
		System.out.println("water leak: "+water_leak_time+" has leak: "+hasWaterLeak());
		System.out.println("NP amount: "+NP_amt);
		System.out.println("NP date: "+NP_date);
		System.out.println("restore date: "+restore);
		System.out.println(ZonedDateTime.now().minusSeconds(LEAK_CLEAR_SECONDS));
	}
}
