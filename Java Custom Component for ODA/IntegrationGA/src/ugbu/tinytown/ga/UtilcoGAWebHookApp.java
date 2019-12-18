/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ugbu.tinytown.ga;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse;

import ugbu.dss.demo.c2mapi.ACHBillPayInvoker;
import ugbu.dss.demo.c2mapi.AccountSummaryInvoker;
import ugbu.dss.demo.c2mapi.AccountSummaryResults;
import ugbu.dss.demo.c2mapi.AutoPaySetupInvoker;
import ugbu.dss.demo.c2mapi.HighBillPreferenceInvoker;
import ugbu.dss.demo.c2mapi.PreferenceResults;
import ugbu.dss.demo.c2mapi.RebateStatusInvoker;
import ugbu.dss.demo.c2mapi.RebateSubmitInvoker;
import ugbu.dss.demo.nest.NestInvoker;
import ugbu.dss.demo.ofsc.OFSCScheduler;

public class UtilcoGAWebHookApp extends DialogflowApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtilcoGAWebHookApp.class);

	// hard code to Gina Elliot on sphinx.


	
	public static final String ACCT_ID = "9940491655";
	public static final String PER_ID = "3682185931";
	public static final String PREMISE_ID = "4265024815";
	
	public static final String EMAIL = "gina@tinytown.com";
	public static final String NAME = "Bertrand";

/*	
	// hard code to John Elliot on sphinx.
	
	public static final String ACCT_ID = "0794337011";
	public static final String PER_ID = "4119807474";
	public static final String PREMISE_ID = "6505546551";
	
	public static final String EMAIL = "gina@tinytown.com";
	public static final String NAME = "John";

	*/
	
	public static final String NC2M_ACCT_ID = "4518548137";  
	
	public static final String NC2M_ACCT_DNP = "2035172533";  //SOMTinyTownDNP
	public static final String NC2M_ACCT_LEAK = "4518548137";  // Anthony,Scott,
	public static final String NC2M_ACCT_NORMAL = "6583954288"; // antonio popa
	

	private static final String CTX_BILL = "bill";
	private static final String CTX_PARAM_BILL_AMOUNT = "ctx_bill_amount";
	
	private static final String PARAM_TEMP = "temperature";
	private static final String PARAM_PAY_AMOUNT = "param_pay_amount";
	private static final String PARAM_HEA_SLOT = "param_hea_slot.original";
												  
	
	
	public static final int MIN_TEMP = 70;
	public static final int MAX_TEMP = 90;
	public static final boolean SUMMER = (LocalDate.now().getMonthValue() > Month.APRIL.getValue()) && (LocalDate.now().getMonthValue() < Month.OCTOBER.getValue());
	

	public static  ResourceBundle rb;
	public static  NumberFormat cf;
	public static  DateTimeFormatter df;
	
	
	
	public static ResourceBundle getRB(Locale locale)
	{
		rb = ResourceBundle.getBundle("OracleUtilities", locale);
		cf = NumberFormat.getCurrencyInstance(locale);
		df = DateTimeFormatter.ofPattern("EEEE, MMMM   dd.").withLocale(locale);
		return rb;
	}
	
	@ForIntent("Default Welcome Intent")
	public ActionResponse welcome(ActionRequest request) {
		LOGGER.info("Default Welcome Intent start. ["+request.getLocale().getDisplayLanguage()+"]");
		rb = getRB(request.getLocale());
		
		//String alert = ActionsServlet.getMockStatus("acct"+(new Random().nextInt(6)+1));
				
		String alert = ActionsServlet.getStatus(NC2M_ACCT_ID);
		//String alert ="Normal";
	
		
		String text = ((alert.equals("Normal")) ? "" : alert) + String.format(rb.getString("welcome.prompt"),NAME)+rb.getString("welcome.message");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(text))
				.add(VisualResponses.getAlert(rb,alert)).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("Default Welcome Intent.");
		return actionResponse;
	}

	@ForIntent("upcoming_bills")
	public ActionResponse upcoming_bills(ActionRequest request) {
		LOGGER.info("upcoming_bills start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder
				.add(new SimpleResponse()
						.setTextToSpeech(rb.getString("upcoming.text")))
				.add(VisualResponses.getBillList(rb)).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("bill_pay end.");
		return actionResponse;
	}

	@ForIntent("energy_bill")
	public ActionResponse energy_bill(ActionRequest request) {
		LOGGER.info("energy_bill start.");

		AccountSummaryResults results = AccountSummaryInvoker.getInstance().invoke(ACCT_ID, PER_ID);

		String response = String.format(rb.getString("bill.text"),cf.format(results.dueAmount), results.duedate.format(df));

		// set the bill amount in context
		LOGGER.info(request.getContexts().toString());

		ActionContext ac = new ActionContext(CTX_BILL, 10);
		HashMap<String, String> param = new HashMap<String, String>();
		param.put(CTX_PARAM_BILL_AMOUNT, String.valueOf(results.dueAmount));
		ac.setParameters(param);

		ActionResponse actionResponse = getResponseBuilder(request).add(response).add(VisualResponses.getEnergyBill(rb,cf,df,results))
				.addSuggestions(new String[] { rb.getString("nudges.pay"), rb.getString("nudges.different") }).add(ac).build();

		LOGGER.info(actionResponse.toString());
		LOGGER.info("energy_bill end.");
		return actionResponse;
	}

	@ForIntent("bill_compare")
	public ActionResponse bill_compare(ActionRequest request) {
		LOGGER.info("bill_compare start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(rb.getString("compare.text")))
				.add(VisualResponses.getBillCompare(rb,cf))
				.addSuggestions(new String[] { rb.getString("nudges.pay"), rb.getString("nudges.save") }).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("bill_compare end.");
		return actionResponse;
	}

	@ForIntent("bill_pay")
	public ActionResponse bill_pay(ActionRequest request) {
		LOGGER.info("bill_pay start.");

		LOGGER.info(request.getContexts().toString());
		ActionContext bill_ctx = request.getContext(CTX_BILL);

		float pay_amt = 1.0f;
		float bill_amt = 0.0f;

		if (bill_ctx != null) {

			Object ctx_param = bill_ctx.getParameters().get(CTX_PARAM_BILL_AMOUNT);
			
			try {
				bill_amt =Float.parseFloat(ctx_param.toString());
			}
			catch(Exception e)
			{
				LOGGER.error("Error parsing: "+ctx_param, e);
				bill_amt=0;
			}

			LOGGER.info("Bill amount: " + bill_amt);

		}

		Object param = request.getParameter(PARAM_PAY_AMOUNT);

		if (param != null && !param.toString().trim().isEmpty()) {
			// LOGGER.info("extracting float: "+ ((Map)param).get("amount"));
			try {
				pay_amt = ((Map<String, Double>) param).get("amount").floatValue();
			}
			catch(Exception e)
			{
				LOGGER.error("Error parsing: "+param, e);
				pay_amt=10;
			}
				
		} else {
			LOGGER.info("No param found, will default to bill amount");
			pay_amt = (bill_amt > 0.0f) ? bill_amt : 1.0f;
		}

		String results = ACHBillPayInvoker.getInstance().invoke(ACCT_ID, PER_ID, pay_amt);


		String response =String.format(rb.getString("pay.text"),cf.format(pay_amt));
				
		
		String[] suggestion = {rb.getString("nudges.save")};
		boolean autopay = true;
		
		// check if we can prompt the user for autopay
		if (AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, false) == false)
		{
			suggestion = new String[] {rb.getString("nudges.autopay"),rb.getString("nudges.save") };
			response += rb.getString("pay.auto");
		}
		else
		{
			autopay = false;
			response += rb.getString("pay.save");
		}

		ActionResponse actionResponse = getResponseBuilder(request).add(response)
				.add(VisualResponses.getPaymentConfirmation(rb,results,autopay)).addSuggestions( suggestion)
				.build();

		LOGGER.info(actionResponse.toString());
		LOGGER.info("bill_pay end.");
		return actionResponse;
	}

	@ForIntent("bill_pay_followup_yes")
	public ActionResponse bill_pay_followup_yes(ActionRequest request) 
	{
		return autopay_signup(request);
	}
	
	@ForIntent("autopay_signup")
	public ActionResponse autopay_signup(ActionRequest request) {
		LOGGER.info("auto signup start.");

		String response = rb.getString("autopay.done");
		if (AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, false) == false)
		{
			AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, true);
		}
		else
		{
			response = rb.getString("autopay.already");
		}
		
		

		String suggestions[] = {rb.getString("nudges.save")};
		boolean highbill = false;
		
		if(!HighBillPreferenceInvoker.getInstance().invoke(ACCT_ID, PER_ID, null).signedup)
		{
			response += rb.getString("autopay.highbill");
			suggestions = new String[]{ rb.getString("nudges.save"), rb.getString("nudges.highbill")};
		}
		ActionResponse actionResponse = getResponseBuilder(request).add(response)
				.add(VisualResponses.getAutopayConfirmation(rb,highbill)).addSuggestions(suggestions)
				.build();

		LOGGER.info(actionResponse.toString());
		LOGGER.info("autopay signup end.");
		return actionResponse;
	}
	
	@ForIntent("autopay_followup_yes")
	public ActionResponse autopay_followup_yes(ActionRequest request) {
		return highbill_signup(request);
	}
	
	
	@ForIntent("highbill_signup")
	public ActionResponse highbill_signup(ActionRequest request) {
		LOGGER.info("high bill signup start.");

		PreferenceResults result = HighBillPreferenceInvoker.getInstance().invoke(ACCT_ID, PER_ID, null);
		
		
		String response = rb.getString("highbill.done");
		if (!result.signedup)
			HighBillPreferenceInvoker.getInstance().invoke(ACCT_ID, PER_ID, result);
		else
			response = rb.getString("highbill.already");
			
		ActionResponse actionResponse = getResponseBuilder(request).add(response)
				.add(VisualResponses.getHighBillConfirmation(rb, result)).addSuggestions(new String[] { rb.getString("nudges.save")})
				.build();

		LOGGER.info(actionResponse.toString());
		LOGGER.info("high bill signup end.");
		return actionResponse;
	}
	
	
	
	@ForIntent("tip_peak_followup_yes")
	public ActionResponse tip_peak_followup_yes(ActionRequest request) {
		return peak_signup(request);
	}
	
	@ForIntent("peak_signup")
	public ActionResponse peak_signup(ActionRequest request) {
		LOGGER.info("peak signup start.");

		int enrolled =  RebateStatusInvoker.getInstance().invoke(ACCT_ID, PER_ID, PREMISE_ID);
		String conf = null;
		String response = rb.getString("peak.done");
		if (enrolled == 0)
		{
			conf = RebateSubmitInvoker.getInstance().invoke(ACCT_ID, PER_ID, PREMISE_ID);
		}
		else
			response = rb.getString("peak.already");

		ActionResponse actionResponse = getResponseBuilder(request).add(response)
				.add(VisualResponses.getPeakConfirmation(rb,enrolled,conf)).addSuggestions(new String[] { rb.getString("nudges.save") })
				.build();

		LOGGER.info(actionResponse.toString());
		LOGGER.info("peak_signup end.");
		return actionResponse;
	}

	
	@ForIntent("hea_schedule_query")
	public ActionResponse hea_schedule_query(ActionRequest request) {
		LOGGER.info("hea scdedule query start.");

		
		OFSCScheduler ofsc = OFSCScheduler.getInstance();
		String date = ofsc.getAvailableDate();
		List<String> sch = 	ofsc.getAvailableSchedules(date);
		
		String response = rb.getString("hea.online");
		String suggestions[] = {rb.getString("nudges.save")};
		
		if (sch.size() == 1) {
			response = String.format(rb.getString("hea.shed1"), sch.get(0));
			suggestions = new String[] {rb.getString("nudges.save"), sch.get(0).toString()};
		}
		else if (sch.size() >=2) {
			response = String.format(rb.getString("hea.shed2"), sch.get(0),sch.get(1));
			suggestions = new String[] {sch.get(0).toString(), sch.get(1).toString()};
		}
	

		ActionResponse actionResponse = getResponseBuilder(request).add(response)
				.add(VisualResponses.getHeaSchedule(df,date, sch)).addSuggestions(suggestions)
				.build();

		LOGGER.info("hea scdedule query start.");
		return actionResponse;
	}

	@ForIntent("hea_schedule_signup")
	public ActionResponse hea_signup(ActionRequest request) {
		LOGGER.info("hea signup start.");
		
		
		
		Object param = request.getContexts().get(0).getParameters().get(PARAM_HEA_SLOT);
		String slot="";
		if (param != null && !param.toString().trim().isEmpty()) {
			slot = param.toString();
			LOGGER.info("scheduling for: "+slot);
				
		} else {
			LOGGER.info("No param found, will default: ");
			LOGGER.info("param: "+request.getParameter("param_hea_slot"));
			LOGGER.info("param original: "+request.getParameter("param_hea_slot.original"));
		
			LOGGER.info("argument: "+request.getArgument("param_hea_slot"));
			LOGGER.info("argument original: "+request.getArgument("param_hea_slot.original"));
			
			LOGGER.info("rawtest: "+request.getRawText());
			
			LOGGER.info("context "+request.getContexts().size());
			for(ActionContext c : request.getContexts()) {
				LOGGER.info("["+c.getName()+"]parameter keyset: "+c.getParameters().keySet());
			}
		}
		
		
		return _hea_signup(request, slot);
	}
	
	public ActionResponse _hea_signup(ActionRequest request,String slot) {
		LOGGER.info("hea signup internal  start.");
		
		
		OFSCScheduler ofsc = OFSCScheduler.getInstance();
		String date = ofsc.getAvailableDate();
		String sdate = LocalDate.parse(date).format(df);
		List<String> sch = 	ofsc.getAvailableSchedules(date);
		
		if (!sch.contains(slot))
		{
			LOGGER.info("Requested SLOT "+slot+" not found, using first availble slot");
			slot = sch.get(0);
		}
		
		boolean flag = ofsc.bookAppointment(date, slot);
		
		ActionResponse actionResponse = getResponseBuilder(request).add(flag ? String.format(rb.getString("hea.done"),slot,sdate):rb.getString("hea.err"))
				.add(VisualResponses.getHeaSignup(rb, sdate,slot, flag)).addSuggestions(new String[] { rb.getString("nudges.save")})
				.build();

		LOGGER.info("hea sighnup internal . end.");
		return actionResponse;
	}
	
	@ForIntent("tstat_set_cooling2")
	public ActionResponse thermostat_cooling2(ActionRequest request) {
		LOGGER.info("thermostat_cooling2 start.");

		int temp = get_setpoint2(request);
		
		
		LOGGER.info("setpoint is resolved to "+temp);


		NestInvoker nest = NestInvoker.getInstance();

		int currtemp = Integer.parseInt(nest.readTargetTemprature());
		int target = (temp < 20) ? currtemp + temp : temp;

		
		ActionResponse actionResponse = set_setpoint(request, nest, target);
				
		LOGGER.info("thermostat_cooling2 stop.");		
		
		return actionResponse;
	}
	
	@ForIntent("tstat_set_cooling")
	public ActionResponse thermostat_cooling(ActionRequest request) {
		LOGGER.info("thermostat_cooling start.");

		int temp = get_setpoint(request);
		
		
		LOGGER.info("setpoint is resolved to "+temp);


		NestInvoker nest = NestInvoker.getInstance();

		int currtemp = Integer.parseInt(nest.readTargetTemprature());
		int target = (temp < 10) ? currtemp + temp : temp;

		
		ActionResponse actionResponse = set_setpoint(request, nest, target);
				
		LOGGER.info("thermostat_cooling stop.");		
		
		return actionResponse;
	}
		
	@ForIntent("tstat_set_heating")
	public ActionResponse thermostat_heating(ActionRequest request) {
		LOGGER.info("thermostat_heating start.");

		int temp = get_setpoint(request);
		
	
		LOGGER.info("setpoint is resolved to "+temp);


		NestInvoker nest = NestInvoker.getInstance();

		int currtemp = Integer.parseInt(nest.readTargetTemprature());
		int target = (temp < 10) ? currtemp - temp : temp;

		
		ActionResponse actionResponse = set_setpoint(request, nest, target);
				
		LOGGER.info("thermostat_heating stop.");		
		
		return actionResponse;
	}
	
	@ForIntent("tstat_set_heating2")
	public ActionResponse thermostat_heating2(ActionRequest request) {
		LOGGER.info("thermostat_heating2 start.");

		int temp = get_setpoint2(request);
		
	
		LOGGER.info("setpoint is resolved to "+temp);


		NestInvoker nest = NestInvoker.getInstance();

		int currtemp = Integer.parseInt(nest.readTargetTemprature());
		int target = (temp < 20) ? currtemp - temp : temp;

		
		ActionResponse actionResponse = set_setpoint(request, nest, target);
				
		LOGGER.info("thermostat_heating2 stop.");		
		
		return actionResponse;
	}
		

	public int  get_setpoint2(ActionRequest request) {
		LOGGER.info("thermostat_set2 start.");

		int temp = 2;
		Object param = request.getParameter(PARAM_TEMP);

		if (param != null && !param.toString().trim().isEmpty()) {
			LOGGER.info("temp is " + param.getClass().getName());
			temp = ((Double)param).intValue();
			//temp = ((Map<String, Double>) param).get("amount").intValue();
			LOGGER.info(" param found:"+temp);
			
		} else {
			LOGGER.info("No param found, will default to 2");
		}

		return temp;
	}

	public int  get_setpoint(ActionRequest request) {
		LOGGER.info("thermostat_set start.");

		int temp = 2;
		Object param = request.getParameter(PARAM_TEMP);

		if (param != null && !param.toString().trim().isEmpty()) {
			LOGGER.info("temp is " + param.getClass().getName());
			//temp = ((Double)param).intValue();
			temp = ((Map<String, Double>) param).get("amount").intValue();
			LOGGER.info(" param found:"+temp);
			
		} else {
			LOGGER.info("No param found, will default to 2");
		}

		return temp;
	}
	public ActionResponse  set_setpoint(ActionRequest request, NestInvoker nest, int target) {
		
		String response = rb.getString("tstat.err");
		try {
			String nestresponse = nest.setTargetTemprature(String.valueOf(target));

			LOGGER.info("Nest Response = " + nestresponse);

			response = String.format(rb.getString("tstat.text"),target);
		}
		catch(Exception e)
		{
			LOGGER.error("error setting the thermostat", e);
		}

		ResponseBuilder responseBuilder = getResponseBuilder(request).add(response);
		ActionResponse actionResponse = responseBuilder		
										.addSuggestions(new String[] { rb.getString("nudges.save") })
										.build();
		return actionResponse;
	}
	
	@ForIntent("tips")
	public ActionResponse tips(ActionRequest request) {
		LOGGER.info("tips start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(
				rb.getString("tips.text")))
				.add(VisualResponses.getTipList(rb,SUMMER))
				// .addSuggestions(new String[] {"Pay my bill","See ways to save"})
				.build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("tips end.");
		return actionResponse;
	}

	@ForIntent("list_select")
	public ActionResponse list_select(ActionRequest request) {
		LOGGER.info("list_select start.");
                    String selectedItem = request.getArgument("OPTION").getTextValue();

		LOGGER.info("OPTION : " + selectedItem);
		if (selectedItem.equalsIgnoreCase("bill"))
			return energy_bill(request);

		if (selectedItem.equalsIgnoreCase("ev"))
			return ev_tip(request);
		
		if (selectedItem.equalsIgnoreCase("hea"))
			return hea_tip(request);


		if (selectedItem.equalsIgnoreCase("tstat"))
			return tstat_tip(request);

		if (selectedItem.equalsIgnoreCase("ptr"))
			return peak_tip(request);

		if (selectedItem.endsWith("p.m.") || selectedItem.endsWith("a.m."))
			return _hea_signup(request, selectedItem);
		
		LOGGER.info("list_select . no option found.");
		return getResponseBuilder(request).add("This option is not yet implemented").build();

	}

	@ForIntent("tip_tstat")
	public ActionResponse tstat_tip(ActionRequest request) {
		
		return SUMMER ?tstat_tip_summer(request) : tstat_tip_winter(request);
	}

	
	public ActionResponse tstat_tip_summer(ActionRequest request) {
		LOGGER.info("tstat_tip_summer start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);

		int curTemp = Integer.parseInt(NestInvoker.getInstance().readTargetTemprature());

		String tip = String.format(rb.getString("tip.ststat.current"), curTemp) ;
		String suggestions[]  = new String[] { rb.getString("tip.ststat.raise"), rb.getString("nudges.save") };
		if(curTemp < MAX_TEMP)
		{
			tip +=rb.getString("tip.ststat.explain");
		}
		else
		{
			tip +=rb.getString("tip.ststat.already");
			suggestions  = new String[] {rb.getString("nudges.save") };
		}
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(tip))
				.add(VisualResponses.getTstatTipCooling(rb,curTemp, MIN_TEMP, MAX_TEMP))
				.addSuggestions(suggestions).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("tstat_tip_summer end.");
		return actionResponse;
	}
	
	public ActionResponse tstat_tip_winter(ActionRequest request) {
		LOGGER.info("tstat_tip_winter start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);

		int curTemp = Integer.parseInt(NestInvoker.getInstance().readTargetTemprature());

		String tip =  String.format(rb.getString("tip.wtstat.current"), curTemp) ;
		String suggestions[]  = new String[] { rb.getString("tip.wtstat.lower"), rb.getString("nudges.save") };
		if(curTemp > MIN_TEMP)
		{
			tip +=rb.getString("tip.wtstat.explain");;
		}
		else
		{
			tip +=rb.getString("tip.ststat.already");
			suggestions  = new String[] {rb.getString("nudges.save") };
		}
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(tip))
				.add(VisualResponses.getTstatTipHeating(rb,curTemp, MIN_TEMP, MAX_TEMP))
				.addSuggestions(suggestions).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info("tstat_tip_winter end.");
		return actionResponse;
	}
	
	@ForIntent("tip_ev")
	public ActionResponse ev_tip(ActionRequest request) {
		LOGGER.info(" ev_tip start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(
				rb.getString("tip.ev.text")))
				.addSuggestions(new String[] { rb.getString("nudges.save") }).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info(" ev_tip end.");
		return actionResponse;
	}

	
	@ForIntent("tip_hea")
	public ActionResponse hea_tip(ActionRequest request) {
		LOGGER.info(" hea_tip start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(
				rb.getString("tip.hea.text")))
				.add(VisualResponses.getAuditTip(rb))
				.addSuggestions(new String[] { rb.getString("nudges.save") , rb.getString("nudges.audit") }).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info(" hea_tip end.");
		return actionResponse;
	}

	@ForIntent("tip_peak")
	public ActionResponse peak_tip(ActionRequest request) {
		LOGGER.info(" peak_tip start.");

		ResponseBuilder responseBuilder = getResponseBuilder(request);
		ActionResponse actionResponse = responseBuilder.add(new SimpleResponse().setTextToSpeech(
				rb.getString("tip.peak.text")))
				.add(VisualResponses.getPeakTip(rb))
				.addSuggestions(new String[] { rb.getString("nudges.save"), rb.getString("nudges.peak") }).build();
		LOGGER.info(actionResponse.toString());
		LOGGER.info(" peak_tip end.");
		return actionResponse;
	}
}
