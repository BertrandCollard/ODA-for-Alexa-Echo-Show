package ugbu.dss.demo.c2mapi;

public class PreferenceResults {
	public String name;
	public String contactid;
	public String contact;
	public boolean signedup;
	public String xref;
	
	public PreferenceResults()
	{}
	
	public void print()
	{
		System.out.println("Preference: "+name);
		System.out.println("contact_id: "+contactid);
		System.out.println("contact: "+contact);
		System.out.println("in use: "+signedup);
		System.out.println("xref: "+xref);
	}
}
