package ugbu.dss.demo.c2mapi;

import java.time.LocalDate;

public class AccountSummaryResults {
	public LocalDate duedate;
	public float dueAmount;
	public float prev;
	public float cur;
	public float payments;
	
	public AccountSummaryResults()
	{
		
	}
	
	public void print()
	{
		System.out.println("Bill Due Date:"+duedate);
		System.out.println("previous balance:"+prev);
		System.out.println("Bill  Amount:"+cur);
		System.out.println("payments:"+payments);
		System.out.println("due:"+dueAmount);
	}
}
