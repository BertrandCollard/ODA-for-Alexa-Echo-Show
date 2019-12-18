package ugbu.dss.demo.c2mapi;

import java.time.LocalDate;

public class BillViewResults {
	public LocalDate duedate;
	public float dueAmount;
	
	public BillViewResults()
	{
		
	}
	
	public void print()
	{
		System.out.println("Bill Due Date:"+duedate.toString());
		System.out.println("Bill Due Amout:"+dueAmount);
	}
}
