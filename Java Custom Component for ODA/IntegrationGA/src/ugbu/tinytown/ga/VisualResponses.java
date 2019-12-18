package ugbu.tinytown.ga;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.google.actions.api.response.helperintent.SelectionList;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.ListSelectListItem;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.TableCard;
import com.google.api.services.actions_fulfillment.v2.model.TableCardCell;
import com.google.api.services.actions_fulfillment.v2.model.TableCardColumnProperties;
import com.google.api.services.actions_fulfillment.v2.model.TableCardRow;

import ugbu.dss.demo.c2mapi.ACHBillPayInvoker;
import ugbu.dss.demo.c2mapi.AccountSummaryResults;
import ugbu.dss.demo.c2mapi.PreferenceResults;

public final  class VisualResponses {
	
	
	public static BasicCard getAlert(ResourceBundle rb, String alert)
	{
		BasicCard card =  new BasicCard();
		
				if(alert.equals("Normal"))
				{
					card.setTitle(String.format(rb.getString("welcome.prompt"),UtilcoGAWebHookApp.NAME))
						.setFormattedText(rb.getString("welcome.message"));
				}
				else
				{
					card.setTitle(rb.getString("welcome.alert"))
					.setFormattedText(alert);
				}
						
		return card;				
	}
	
	public static 	SelectionList getTipList(ResourceBundle rb, boolean summer)
	{
		ArrayList<ListSelectListItem> items = new ArrayList<ListSelectListItem>();
		
		
		
	
		items.add(new ListSelectListItem()		
				.setTitle(rb.getString("tips.peak.title"))
				.setDescription(rb.getString("tips.peak.save"))
				.setImage(new Image()
						.setUrl("https://www.dropbox.com/s/bybqccw01mhpdm2/GA_Tip_Peak_Time.png?dl=1")
						.setHeight(50)
						.setWidth(50)
						.setAccessibilityText("Sign up for peak rewards program")
						)
				.setOptionInfo(new OptionInfo()
						.setKey("ptr"))
				);
		

		items.add(new ListSelectListItem()		
				.setTitle(rb.getString("tips.hea.title"))
				.setDescription(rb.getString("tips.hea.save"))
				.setImage(new Image()
						.setUrl("https://www.dropbox.com/s/toorsfxxshorewo/GA_Tip_home_energy_audit.png?dl=1")
						.setAccessibilityText("Schedule an professional Home Energy Audit")
						.setHeight(50)
						.setWidth(50)
						)
				.setOptionInfo(new OptionInfo()
						.setKey("hea"))
				);
		
		String title = rb.getString(summer?"tips.ststat.title":"tips.wtstat.title");	
		items.add(new ListSelectListItem()		
				.setTitle(title)
				.setDescription(rb.getString(summer?"tips.ststat.save":"tips.wtstat.save"))
				.setImage(new Image()
						.setUrl("https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png")
						.setAccessibilityText(title)
						.setHeight(50)
						.setWidth(50)
						)
				.setOptionInfo(new OptionInfo()
						.setKey("tstat"))
				);
		/* ------
		items.add(new ListSelectListItem()		
				.setTitle("Charge your electric vehicle overnight")
				.setDescription("Save up to $100 a year")
				.setImage(new Image()
						.setUrl("https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip439_charge_electric_vehicles_wisely.png")
						.setAccessibilityText("Charge your electric vehicle overnight")
						.setHeight(50)
						.setWidth(50)
						)
				.setOptionInfo(new OptionInfo()
						.setKey("ev"))
				);
		*/
		return new SelectionList()
					.setTitle(rb.getString("tips.title"))
					.setItems(items);
	}
	
	
	public static 	SelectionList getBillList(ResourceBundle rb)
	{
		ArrayList<ListSelectListItem> items = new ArrayList<ListSelectListItem>();
		
		items.add(new ListSelectListItem()		
				.setTitle(rb.getString("upcoming.bank"))
				.setDescription("...6417")
				.setImage(new Image()
						.setUrl("https://fidoalliance.org/wp-content/uploads/bankofamerica.png")
						.setAccessibilityText("Bank of America Travel Rewards Visa Signature")
						)
				.setOptionInfo(new OptionInfo()
						.setKey("boa"))
				);
				
		items.add(new ListSelectListItem()		
				.setTitle(rb.getString("upcoming.utility"))
				.setDescription("...3462")
				.setImage(new Image()
						.setUrl("https://s3.amazonaws.com/cbalane-ihd-demo/assets/utilityco_logo_192x192.png?v=4")
						.setAccessibilityText("Utilitity Co")
						)
				.setOptionInfo(new OptionInfo()
						.setKey("bill"))
				);
		
		return new SelectionList()
					.setTitle(rb.getString("upcoming.title"))
					.setItems(items);
	}
	
	public static TableCard getEnergyBill(ResourceBundle rb, NumberFormat cf, DateTimeFormatter df, AccountSummaryResults summ)
	{
		ArrayList<TableCardRow> rows = new ArrayList<TableCardRow>();
		
	    ArrayList<TableCardCell> prev_row = new ArrayList<TableCardCell>();
	    
		prev_row.add(new TableCardCell().setText(rb.getString("bill.prev")));
		prev_row.add(new TableCardCell().setText(cf.format(summ.prev)));
		
		ArrayList<TableCardCell> bill_row = new ArrayList<TableCardCell>();
	    
		bill_row.add(new TableCardCell().setText(rb.getString("bill.cur")));
		bill_row.add(new TableCardCell().setText(cf.format(summ.cur)));
		
		ArrayList<TableCardCell> pay_row = new ArrayList<TableCardCell>();
	    
		pay_row.add(new TableCardCell().setText(rb.getString("bill.pay")));
		pay_row.add(new TableCardCell().setText(cf.format(summ.payments)));
		
		ArrayList<TableCardCell> due_row = new ArrayList<TableCardCell>();
	    
		due_row.add(new TableCardCell().setText(rb.getString("bill.due")));
		due_row.add(new TableCardCell().setText(cf.format(summ.dueAmount)));
		
		rows.add( new TableCardRow().setCells(prev_row).setDividerAfter(Boolean.FALSE));
		rows.add( new TableCardRow().setCells(bill_row).setDividerAfter(Boolean.FALSE));
		rows.add( new TableCardRow().setCells(pay_row).setDividerAfter(Boolean.TRUE));
		rows.add( new TableCardRow().setCells(due_row).setDividerAfter(Boolean.TRUE));
		
		
		ArrayList<TableCardColumnProperties> cols = new ArrayList<TableCardColumnProperties>();
		cols.add( new TableCardColumnProperties().setHorizontalAlignment("LEADING"));
		cols.add( new TableCardColumnProperties().setHorizontalAlignment("TRAILING"));
		
		return new TableCard()
				.setTitle(String.format(rb.getString("bill.date"),summ.duedate.format(df)))
				.setSubtitle(rb.getString("bill.high"))
				.setRows(rows)
				.setColumnProperties(cols);
						
	}
	
	public static BasicCard getTstatTipCooling(ResourceBundle rb,int curTemp, int min, int max)
	{
		String tip = String.format(rb.getString("tip.ststat.current2"), curTemp);
		if(curTemp >= max)
			tip = String.format(rb.getString("tip.ststat.already2"), curTemp);
			
		return new BasicCard()
				.setTitle(rb.getString("tip.ststat.title"))
				.setFormattedText(tip)
				.setImage(new Image()
						.setHeight(100)
						.setWidth(100)
						.setUrl("https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png")
						.setAccessibilityText("Schedule thermostat to be set at 68 degrees"))
				//.setImageDisplayOptions("WHITE").
				;
						
	}
	
	public static BasicCard getTstatTipHeating(ResourceBundle rb, int curTemp, int min, int max)
	{
		String tip = String.format(rb.getString("tip.wtstat.current2"), curTemp);
		if(curTemp <= min)
			tip = String.format(rb.getString("tip.wtstat.already2"), curTemp);
			
		return new BasicCard()
				.setTitle(rb.getString("tip.ststat.title"))
				.setFormattedText(tip)
				.setImage(new Image()
						.setHeight(100)
						.setWidth(100)
						.setUrl("https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png")
						.setAccessibilityText("Schedule thermostat to be set at 78 degrees"))
				//.setImageDisplayOptions("WHITE").
				;
						
	}
	
	public static BasicCard getPeakTip(ResourceBundle rb)
	{
		String tip = rb.getString("tip.peak.desc");
						
		return new BasicCard()
				.setTitle(rb.getString("tip.peak.title"))
				.setSubtitle(rb.getString("tip.peak.save"))
				.setFormattedText(tip)
				.setImage(new Image()
						.setUrl("https://www.dropbox.com/s/bybqccw01mhpdm2/GA_Tip_Peak_Time.png?dl=1")
						.setHeight(100)
						.setWidth(100)
						.setAccessibilityText("Sign up for Peak Rewards Days"));
	}
	
	public static BasicCard getAuditTip(ResourceBundle rb)
	{
		String tip = rb.getString("tip.hea.desc");
				

		return new BasicCard()
				.setTitle(rb.getString("tip.hea.title"))
				.setSubtitle(rb.getString("tip.hea.save"))
				.setFormattedText(tip)
				.setImage(new Image()
						.setUrl("https://www.dropbox.com/s/toorsfxxshorewo/GA_Tip_home_energy_audit.png?dl=1")
						.setHeight(100)
						.setWidth(100)
						.setAccessibilityText("Schedule an Home Energy Audit"));
	}
	public static 	SelectionList getHeaSchedule(DateTimeFormatter df, String date, List<String> sch)
	{
		ArrayList<ListSelectListItem> items = new ArrayList<ListSelectListItem>();
		
		for (String slot: sch)
		{
			items.add(new ListSelectListItem()		
				.setTitle(slot)
				.setOptionInfo(new OptionInfo()
						.setKey(slot))
				);
				
		}
		return new SelectionList()
					.setTitle(LocalDate.parse(date).format(df))
					.setItems(items);
	}
	public static BasicCard getAutopayConfirmation(ResourceBundle rb, boolean highbill)
	{
		return new BasicCard()
				.setTitle(rb.getString("autopay.title"))
				.setSubtitle(rb.getString("autopay.conf")+ACHBillPayInvoker.ACCT_NO)
				.setFormattedText(rb.getString("autopay.explain")
						+ ((highbill)? rb.getString("autopay.highbill"):"")	
						);
						
	}
	public static BasicCard getHeaSignup(ResourceBundle rb, String date, String slot, boolean flag )
	{
		if (flag)
			return new BasicCard()
				.setTitle(rb.getString("hea.title"))
				.setSubtitle(slot +" on "+ date)
				.setFormattedText(rb.getString("hea.remind"))
				;
		else
			return new BasicCard()
					.setTitle("Home Energy Audit")
					.setSubtitle(rb.getString("hea.err"))
					.setFormattedText(rb.getString("hea.err2"))
					; 
						
	}
	
	public static BasicCard getHighBillConfirmation(ResourceBundle rb,  PreferenceResults result)
	{
		String text = (result.signedup)? rb.getString("highbill.already2"):
			String.format(rb.getString("highbill.explain"),result.contact);
			
		return new BasicCard()
				.setTitle(rb.getString("highbill.title"))
				.setSubtitle(rb.getString("highbill.conf"))
				.setFormattedText(text);
						
	}
	public static BasicCard getPeakConfirmation(ResourceBundle rb, int enrolled, String conf)
	{
		return new BasicCard()
				.setTitle(rb.getString("peak.title"))
				.setSubtitle(rb.getString((enrolled == 0) ?"peak.conf" :"peak.already2"))
				.setFormattedText(rb.getString("peak.explain"))	;
						
	}
	public static BasicCard getPaymentConfirmation(ResourceBundle rb, String ref, boolean autopay)
	{
		return new BasicCard()
				.setTitle(rb.getString("pay.paid"))
				.setSubtitle(rb.getString("pay.acct")+ACHBillPayInvoker.ACCT_NO)
				.setFormattedText(String.format(rb.getString("pay.conf"),ref)
						+ ((autopay)?rb.getString("pay.auto"):"")	
						);
						
	}
	public static TableCard getBillCompare(ResourceBundle rb, NumberFormat cf)
	{
		ArrayList<TableCardRow> rows = new ArrayList<TableCardRow>();
		
	    ArrayList<TableCardCell> ev_row = new ArrayList<TableCardCell>();
	    
		ev_row.add(new TableCardCell().setText(rb.getString("compare.ev")));
		ev_row.add(new TableCardCell().setText(rb.getString("compare.high")));
		ev_row.add(new TableCardCell().setText("+"+cf.format(19.73)));
		
		ArrayList<TableCardCell> weather_row = new ArrayList<TableCardCell>();
	    
		weather_row.add(new TableCardCell().setText(rb.getString("compare.weather")));
		weather_row.add(new TableCardCell().setText(rb.getString("compare.same")));
		weather_row.add(new TableCardCell().setText("+"+cf.format(0.10)));
		
		ArrayList<TableCardCell> bp_row = new ArrayList<TableCardCell>();
	    
		bp_row.add(new TableCardCell().setText(rb.getString("compare.period")));
		bp_row.add(new TableCardCell().setText(rb.getString("compare.same")));
		bp_row.add(new TableCardCell().setText("+"+cf.format(1.73)));
			
		rows.add( new TableCardRow().setCells(ev_row).setDividerAfter(Boolean.TRUE));
		rows.add( new TableCardRow().setCells(weather_row).setDividerAfter(Boolean.TRUE));
		rows.add( new TableCardRow().setCells(bp_row).setDividerAfter(Boolean.TRUE));
		
		
		ArrayList<TableCardColumnProperties> cols = new ArrayList<TableCardColumnProperties>();
		cols.add( new TableCardColumnProperties().setHorizontalAlignment("LEADING"));
		cols.add( new TableCardColumnProperties().setHorizontalAlignment("CENTER"));
		cols.add( new TableCardColumnProperties().setHorizontalAlignment("TRAILING"));
		
		return new TableCard()
				.setTitle(rb.getString("compare.why"))
				.setRows(rows)
				.setColumnProperties(cols);
		}

}
