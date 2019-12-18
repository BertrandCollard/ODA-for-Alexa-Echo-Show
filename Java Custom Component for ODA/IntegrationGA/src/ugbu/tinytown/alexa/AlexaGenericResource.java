package ugbu.tinytown.alexa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.actions.api.response.helperintent.SelectionList;
import com.google.api.services.actions_fulfillment.v2.model.ListSelect;
import com.google.api.services.actions_fulfillment.v2.model.ListSelectListItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.NumberFormat;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.LocaleUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import ugbu.tinytown.ga.ActionsServlet;
import ugbu.tinytown.ga.UtilcoGAWebHookApp;
import ugbu.tinytown.ga.VisualResponses;

@Path("alexa")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlexaGenericResource {
    @Context
    ServletContext servletContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(AlexaGenericResource.class);

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

    public static final String NC2M_ACCT_DNP = "2035172533"; //SOMTinyTownDNP
    public static final String NC2M_ACCT_LEAK = "4518548137"; // Anthony,Scott,
    public static final String NC2M_ACCT_NORMAL = "6583954288"; // antonio popa


    private static final String CTX_BILL = "bill";
    private static final String CTX_PARAM_BILL_AMOUNT = "ctx_bill_amount";

    private static final String PARAM_TEMP = "temperature";
    private static final String PARAM_PAY_AMOUNT = "param_pay_amount";
    private static final String PARAM_HEA_SLOT = "param_hea_slot.original";


    public static final int MIN_TEMP = 70;
    public static final int MAX_TEMP = 90;
    public static final boolean SUMMER =
        (LocalDate.now().getMonthValue() > Month.APRIL.getValue()) &&
        (LocalDate.now().getMonthValue() < Month.OCTOBER.getValue());


    public static ResourceBundle rb;
    public static NumberFormat cf;
    public static DateTimeFormatter df;

    private static final String BILL_VARIABLE = "__variable__";
    private static final String ENERGY_BILL_POSTBACK = "postback_energy_bill";
    private static final String BILL_COMPARE_POSTBACK = "postback_bill_compare";
    private static final String KEEP_TURN = "keepTurn";
    public static final String ACTION_NAME = "actionName";
    private static final String CARDCOMP_STATE = "___cardcomp_state___";

    private static final String[] SUPPORTED_ACTIONS = {
        "NONE", "TextReceived", "Selection", "nudges.pay", "nudges.different" };
    private static final String ENERGY_BILL_PROPERTIES =
        "{\"" + BILL_VARIABLE + "\": {\"type\": \"string\", \"required\": true},\n" + "\"" + KEEP_TURN +
        "\": {\"type\": \"boolean\", \"required\": false}}";
    private static final String BILL_COMPARE_PROPERTIES =
        "{\"" + BILL_VARIABLE + "\": {\"type\": \"string\", \"required\": true},\n" + "\"" + KEEP_TURN +
        "\": {\"type\": \"boolean\", \"required\": false}}";
    private static final String BILL_PAY_PROPERTIES =
        "{\"" + CTX_PARAM_BILL_AMOUNT + "\": {\"type\": \"string\", \"required\": true},\n" +
        "\"" + PARAM_PAY_AMOUNT + "\": {\"type\": \"string\", \"required\": true},\n" + "\"" + KEEP_TURN +
        "\": {\"type\": \"boolean\", \"required\": false}}";


    public enum SupportedAction {
        NONE("NONE"), //calls constructor with value 3
        TEXT_RECEIVED("textReceived"), //calls constructor with value 2
        SELECTION("Selection"), //calls constructor with value 1
        NUDGES_PAY("nudges.pay"),
        NUDGES_DIFFERENT("nudges.different"),
        NUDGES_AUTOPAY("nudges.autopay"),
        NUDGES_HIGHBILL("nudges.highbill"),
        NUDGES_AUDIT("nudges.audit"),
        NUDGES_PEAK("nudges.peak"),
        NUDGES_SAVE("nudges.save"); // semicolon needed when fields / methods follow


        private final String levelCode;

        SupportedAction(String levelCode) {
            this.levelCode = levelCode;
        }

        public String getLevelCode() {
            return this.levelCode;
        }

        @Override
        public String toString() {
            return this.levelCode;
        }

        public static String[] getValues() {
            return Arrays.stream(SupportedAction.values())
                         .map(Enum::toString)
                         .toArray(String[]::new);
        }

        public static String list() {
            String result = "[";
            for (SupportedAction re : SupportedAction.values()) {
                result += "\"" + re.getLevelCode() + "\",";
            }

            if (result.length() > 1)
                result = result.substring(0, result.length() - 1);
            result += "]";
            return result;
        }

        public static SupportedAction getEnum(String value) {
            for (SupportedAction re : SupportedAction.values()) {
                if (re.levelCode.compareTo(value) == 0) {
                    return re;
                }
            }
            throw new IllegalArgumentException("Invalid SupportedAction value: " + value);
        }

    }


    public static ResourceBundle getRB(Locale locale) {
        String FILENAME = "resources/OracleUtilities";
        rb = ResourceBundle.getBundle(FILENAME, locale);
        cf = NumberFormat.getCurrencyInstance(locale);
        df = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale);
        return rb;
    }

    public AlexaGenericResource() {
    }


    @POST
    @Path("services/welcome")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String welcome(String requestJson) {

        // Provide method implementation.
        // TODO
        Request request = initFromRequest(requestJson);

        //String alert = ActionsServlet.getMockStatus("acct"+(new Random().nextInt(6)+1));

        String alert = ActionsServlet.getStatus(NC2M_ACCT_ID);
        //String alert ="Normal";


        String text =
            ((alert.equals("Normal")) ? "" : alert + " ") + String.format(rb.getString("welcome.prompt"), NAME) + " " +
            rb.getString("welcome.message");
        //System.out.println("Default Welcome Intent: " + rb != null ? rb.getBaseBundleName() : "unknowed");
        System.out.println(text);
        Response response = new Response(request);
        response.reply(request, text);

        if (isAlexaCaller(request)) {
            InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/welcome_ui.json");
            InputStream welcome_data = servletContext.getResourceAsStream("/WEB-INF/resources/welcome_data.json");

            Raw raw = new Raw();
            try {
                String data = readFromInputStream(welcome_data);
                data =
                    data.replace("__TITLE__", String.format(rb.getString("welcome.prompt"), UtilcoGAWebHookApp.NAME))
                    .replace("__SUBTITLE__", rb.getString("welcome.message"));
                raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
            } catch (IOException e) {
            }
            response.reply(request, raw);
        }

        response.transition();
        ObjectMapper Obj = new ObjectMapper();
        return (responseAsString(response));
    }


    @POST
    @Path("services/upcoming_bills")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String upcoming_bills(String requestJson) {

        // Provide method implementation.
        // TODO

        System.out.println("upcoming_bills start.");

        Request request = initFromRequest(requestJson);

        String text = rb.getString("upcoming.text");

        Response response = new Response(request);
        response.reply(request, text);
        SelectionList list = VisualResponses.getBillList(rb);
        ListSelect listSelect = (ListSelect) list.getParameters().get("listSelect");
        CardConversation cardConversation = new CardConversation();
        cardConversation.setLayout("horizontal");
        for (ListSelectListItem item : listSelect.getItems()) {
            Card card = new Card();
            card.setTitle(item.getTitle());
            card.setDescription(item.getDescription());
            card.setImageUrl(item.getImage().getUrl());
            PostbackAction action = new PostbackAction();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode postback = mapper.createObjectNode();
            ((ObjectNode) postback).put(ACTION_NAME, SupportedAction.SELECTION.toString());
            ((ObjectNode) postback).put("id", item.getOptionInfo().getKey());
            action.setPostback(postback);
            action.setLabel(rb.getString("bill.select"));
            card.addAction(action);
            cardConversation.addCard(card);
        }
        response.reply(request, cardConversation);

        if (isAlexaCaller(request)) {
            InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/billlist_ui.json");
            InputStream welcome_data = servletContext.getResourceAsStream("/WEB-INF/resources/billlist_data.json");

            Raw raw = new Raw();
            try {
                String data = readFromInputStream(welcome_data);
                data = data.replace("__TITLE__", String.format(rb.getString("upcoming.title")));
                data = data.replace("__BILL1__", String.format(rb.getString("upcoming.bank")));
                data = data.replace("__BILL2__", String.format(rb.getString("upcoming.utility")));
                raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
            } catch (IOException e) {
            }
            response.reply(request, raw);
        }
        response.transition();
        return (responseAsString(response));
    }

    @POST
    @Path("services/energy_bill")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String energy_bill(String requestJson) {
        System.out.println("energy_bill start.");
        Request request = initFromRequest(requestJson);
        boolean keepTurn = (Boolean) request.getProperties(KEEP_TURN);
        boolean statetracker =
            request.getVariable(CARDCOMP_STATE) != null ? request.getVariable(CARDCOMP_STATE).asBoolean() : false;
        if ((request.asPostback()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            //request.setVariable(BILL_VARIABLE, request.getPostback(ENERGY_BILL_POSTBACK));
            request.setVariable(CTX_PARAM_BILL_AMOUNT, request.getPostback(ENERGY_BILL_POSTBACK));
            Response response = new Response(request);
            response.setKeepTurn(keepTurn);
            response.transition(request.getPostback(ACTION_NAME).asText());
            return (responseAsString(response));
        } else if ((request.asText()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            Response response = new Response(request);
            response.transition(SupportedAction.TEXT_RECEIVED);
            response.setKeepTurn(true);
            return (responseAsString(response));
        } else {
            request.setVariable(CARDCOMP_STATE, true);
            AccountSummaryResults results = AccountSummaryInvoker.getInstance().invoke(ACCT_ID, PER_ID);

            String text =
                String.format(rb.getString("bill.text"), cf.format(results.dueAmount), results.duedate.format(df));

            Response response = new Response(request);
            List<Action> suggestions = new ArrayList<Action>();
            PostbackAction action1 = new PostbackAction();
            action1.setLabel(rb.getString(SupportedAction.NUDGES_PAY.toString()));
            Map<String, Object> postback1 = new HashMap<String, Object>();
            postback1.put(ENERGY_BILL_POSTBACK, Float.toString(results.dueAmount));
            postback1.put(ACTION_NAME, SupportedAction.NUDGES_PAY);
            action1.setPostback(postback1);
            suggestions.add(action1);

            PostbackAction action2 = new PostbackAction();
            action2.setLabel(rb.getString(SupportedAction.NUDGES_DIFFERENT.toString()));
            Map<String, Object> postback2 = new HashMap<String, Object>();
            postback2.put(ENERGY_BILL_POSTBACK, Float.toString(results.dueAmount));
            postback2.put(ACTION_NAME, SupportedAction.NUDGES_DIFFERENT);
            action2.setPostback(postback2);
            suggestions.add(action2);

            response.reply(request, text, suggestions);
            if (isAlexaCaller(request)) {
                InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/energy_bill_ui.json");
                InputStream welcome_data =
                    servletContext.getResourceAsStream("/WEB-INF/resources/energy_bill_data.json");

                Raw raw = new Raw();
                try {
                    String data = readFromInputStream(welcome_data);
                    data =
                        data.replace("__TITLE__", String.format(rb.getString("bill.date"), results.duedate.format(df)))
                        .replace("__SUBTITLE__", rb.getString("bill.high"));
                    data = data.replace("__ITEM1_TITLE__", rb.getString("bill.prev"));
                    data = data.replace("__ITEM1_AMOUNT__", cf.format(results.prev));
                    data = data.replace("__ITEM2_TITLE__", rb.getString("bill.cur"));
                    data = data.replace("__ITEM2_AMOUNT__", cf.format(results.cur));
                    data = data.replace("__ITEM3_TITLE__", rb.getString("bill.pay"));
                    data = data.replace("__ITEM3_AMOUNT__", cf.format(results.payments));

                    raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
                } catch (IOException e) {
                }
                response.reply(request, raw);
            }
            return (responseAsString(response));
        }
    }

    @POST
    @Path("services/bill_pay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String bill_pay(String requestJson) {
        System.out.println("bill_pay start.");
        Request request = initFromRequest(requestJson);
        float pay_amt = 1.0f;
        float bill_amt = 0.0f;
        boolean keepTurn = (Boolean) request.getProperties(KEEP_TURN);
        boolean statetracker =
            request.getVariable(CARDCOMP_STATE) != null ? request.getVariable(CARDCOMP_STATE).asBoolean() : false;
        if ((request.asPostback()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            Response response = new Response(request);
            response.setKeepTurn(keepTurn);
            response.transition(request.getPostback(ACTION_NAME).asText());
            return (responseAsString(response));
        } else if ((request.asText()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            Response response = new Response(request);
            response.transition(SupportedAction.TEXT_RECEIVED);
            response.setKeepTurn(true);
            return (responseAsString(response));
        } else {
            request.setVariable(CARDCOMP_STATE, true);
            String ctx_param =
                request.getVariable(CTX_PARAM_BILL_AMOUNT) != null ?
                request.getVariable(CTX_PARAM_BILL_AMOUNT).asText() : null;
            try {
                bill_amt = Float.parseFloat(ctx_param.toString());
            } catch (Exception e) {
                LOGGER.error("Error parsing: " + ctx_param, e);
                bill_amt = 0;
            }
            //pay_amt = Float.parseFloat((String) request.getProperties(PARAM_PAY_AMOUNT));
            String results = ACHBillPayInvoker.getInstance().invoke(ACCT_ID, PER_ID, pay_amt);


            String text = String.format(rb.getString("pay.text"), cf.format(pay_amt));
            
            Map <String,String> suggestion = new HashMap<String, String>();

            suggestion.put(rb.getString("nudges.save"), SupportedAction.NUDGES_SAVE.getLevelCode());
            
            boolean autopay = true;

            // check if we can prompt the user for autopay
            if (AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, false) == false) {
                suggestion.put(rb.getString("nudges.autopay"), SupportedAction.NUDGES_AUTOPAY.getLevelCode());
                text += rb.getString("pay.auto");
            } else {
                autopay = false;
                text += rb.getString("pay.save");
            }
            Response response = new Response(request);
            response.reply(request, text, suggestion);
            if (isAlexaCaller(request)) {
                String information =
                    String.format(rb.getString("pay.conf"), results) + ((autopay) ? rb.getString("pay.auto") : "");

                replyBasicCard(request, response, rb.getString("pay.paid"),
                               rb.getString("pay.acct") + ACHBillPayInvoker.ACCT_NO, information);
            }
            return (responseAsString(response));
        }
    }

    @POST
    @Path("services/bill_compare")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String bill_compare(String requestJson) {
        System.out.println("bill_compare start.");
        Request request = initFromRequest(requestJson);
        boolean keepTurn = (Boolean) request.getProperties(KEEP_TURN);
        boolean statetracker =
            request.getVariable(CARDCOMP_STATE) != null ? request.getVariable(CARDCOMP_STATE).asBoolean() : false;
        if ((request.asPostback()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            request.setVariable(CTX_PARAM_BILL_AMOUNT, request.getPostback(BILL_COMPARE_POSTBACK));
            Response response = new Response(request);
            response.setKeepTurn(keepTurn);
            response.transition(request.getPostback(ACTION_NAME).asText());
            return (responseAsString(response));
        } else if ((request.asText()) && statetracker) {
            request.setVariable(CARDCOMP_STATE, false);
            Response response = new Response(request);
            response.transition(SupportedAction.TEXT_RECEIVED);
            response.setKeepTurn(true);
            return (responseAsString(response));
        } else {
            request.setVariable(CARDCOMP_STATE, true);
            String billId =
                request.getVariable(BILL_VARIABLE) != null ? request.getVariable(BILL_VARIABLE).asText() : null;
            AccountSummaryResults results = AccountSummaryInvoker.getInstance().invoke(ACCT_ID, PER_ID);
            System.out.println("BillID:" + billId);
            String text = rb.getString("compare.text");
            Response response = new Response(request);

            List<Action> suggestions = new ArrayList<Action>();
            PostbackAction action1 = new PostbackAction();
            action1.setLabel(rb.getString(SupportedAction.NUDGES_PAY.toString()));
            Map<String, Object> postback1 = new HashMap<String, Object>();
            postback1.put(BILL_COMPARE_POSTBACK, Float.toString(results.dueAmount));
            postback1.put(ACTION_NAME, SupportedAction.NUDGES_PAY);
            action1.setPostback(postback1);
            suggestions.add(action1);

            PostbackAction action2 = new PostbackAction();
            action2.setLabel(rb.getString(SupportedAction.NUDGES_SAVE.toString()));
            Map<String, Object> postback2 = new HashMap<String, Object>();
            postback2.put(BILL_COMPARE_POSTBACK, Float.toString(results.dueAmount));
            postback2.put(ACTION_NAME, SupportedAction.NUDGES_SAVE);
            action2.setPostback(postback2);
            suggestions.add(action2);

            response.reply(request, text, suggestions);
            if (isAlexaCaller(request)) {
                InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/bill_compare_ui.json");
                InputStream welcome_data =
                    servletContext.getResourceAsStream("/WEB-INF/resources/bill_compare_data.json");

                Raw raw = new Raw();
                try {
                    String data = readFromInputStream(welcome_data);
                    data = data.replace("__TITLE__", String.format(rb.getString("compare.why")));
                    data = data.replace("__ITEM1_LEADING__", rb.getString("compare.ev"));
                    data = data.replace("__ITEM1_CENTER__", rb.getString("compare.high"));
                    data = data.replace("__ITEM1_TRAILING__", "+" + cf.format(19.73));

                    data = data.replace("__ITEM2_LEADING__", rb.getString("compare.weather"));
                    data = data.replace("__ITEM2_CENTER__", rb.getString("compare.same"));
                    data = data.replace("__ITEM2_TRAILING__", "+" + cf.format(0.10));

                    data = data.replace("__ITEM3_LEADING__", rb.getString("compare.period"));
                    data = data.replace("__ITEM3_CENTER__", rb.getString("compare.same"));
                    data = data.replace("__ITEM3_TRAILING__", "+" + cf.format(1.73));

                    raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
                } catch (IOException e) {
                }
                response.reply(request, raw);
            }
            return (responseAsString(response));
        }
    }

    @POST
    @Path("services/bill_pay_followup_yes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String bill_pay_followup_yes(String requestJson) {
        return autopay_signup(requestJson);
    }

    @POST
    @Path("services/autopay_signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String autopay_signup(String requestJson) {
        LOGGER.info("auto signup start.");
        Request request = initFromRequest(requestJson);

        String text = rb.getString("autopay.done");
        if (AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, false) == false) {
            AutoPaySetupInvoker.getInstance().invoke(ACCT_ID, PER_ID, true);
        } else {
            text = rb.getString("autopay.already");
        }

        String suggestions[] = { rb.getString("nudges.save") };
        boolean highbill = false;

        if (!HighBillPreferenceInvoker.getInstance()
                                      .invoke(ACCT_ID, PER_ID, null)
                                      .signedup) {
            text += rb.getString("autopay.highbill");
            suggestions = new String[] { rb.getString("nudges.save"), rb.getString("nudges.highbill") };
        }

        Response response = new Response(request);
        response.reply(request, text, suggestions);
        if (isAlexaCaller(request)) {
            String information = rb.getString("autopay.explain") + ((highbill) ? rb.getString("autopay.highbill") : "");

            replyBasicCard(request, response, rb.getString("autopay.title"),
                           rb.getString("autopay.conf") + ACHBillPayInvoker.ACCT_NO, information);
        }
        response.setTransition(true);
        return (responseAsString(response));
    }

    @POST
    @Path("services/autopay_followup_yes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String autopay_followup_yes(String requestJson) {
        return highbill_signup(requestJson);
    }

    @POST
    @Path("services/highbill_signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String highbill_signup(String requestJson) {
        LOGGER.info("high bill signup start.");
        Request request = initFromRequest(requestJson);

        PreferenceResults result = HighBillPreferenceInvoker.getInstance().invoke(ACCT_ID, PER_ID, null);


        String text = rb.getString("highbill.done");
        if (!result.signedup)
            HighBillPreferenceInvoker.getInstance().invoke(ACCT_ID, PER_ID, result);
        else
            text = rb.getString("highbill.already");

        Response response = new Response(request);
        response.reply(request, text, new String[] { rb.getString("nudges.save") });
        if (isAlexaCaller(request)) {
            String information =
                (result.signedup) ? rb.getString("highbill.already2") :
                String.format(rb.getString("highbill.explain"), result.contact);
            replyBasicCard(request, response, rb.getString("highbill.title"), rb.getString("highbill.conf"),
                           information);
        }
        response.setTransition(true);
        LOGGER.info("high bill signup end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tip_peak_followup_yes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tip_peak_followup_yes(String requestJson) {
        return peak_signup(requestJson);
    }

    @POST
    @Path("services/peak_signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String peak_signup(String requestJson) {
        LOGGER.info("peak signup start.");
        Request request = initFromRequest(requestJson);

        int enrolled = RebateStatusInvoker.getInstance().invoke(ACCT_ID, PER_ID, PREMISE_ID);
        String conf = null;
        String text = rb.getString("peak.done");
        if (enrolled == 0) {
            conf = RebateSubmitInvoker.getInstance().invoke(ACCT_ID, PER_ID, PREMISE_ID);
        } else
            text = rb.getString("peak.already");
        Response response = new Response(request);
        response.reply(request, text, new String[] { rb.getString("nudges.save") });
        if (isAlexaCaller(request)) {
            String information = rb.getString("peak.explain");
            replyBasicCard(request, response, rb.getString("peak.title"),
                           rb.getString((enrolled == 0) ? "peak.conf" : "peak.already2"), information);
        }
        response.setTransition(true);
        LOGGER.info("peak_signup end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/hea_schedule_query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String hea_schedule_query(String requestJson) {
        LOGGER.info("hea scdedule query start.");
        Request request = initFromRequest(requestJson);
        OFSCScheduler ofsc = OFSCScheduler.getInstance();
        String date = ofsc.getAvailableDate();
        List<String> sch = ofsc.getAvailableSchedules(date);

        String text = rb.getString("hea.online");
        String suggestions[] = { rb.getString("nudges.save") };

        if (sch.size() == 1) {
            text = String.format(rb.getString("hea.shed1"), sch.get(0));
            suggestions = new String[] { rb.getString("nudges.save"), sch.get(0).toString() };
        } else if (sch.size() >= 2) {
            text = String.format(rb.getString("hea.shed2"), sch.get(0), sch.get(1));
            suggestions = new String[] { sch.get(0).toString(), sch.get(1).toString() };
        }

        Response response = new Response(request);
        response.reply(request, text, suggestions);
        if (isAlexaCaller(request)) {
            InputStream hea_schedule_ui = servletContext.getResourceAsStream("/WEB-INF/resources/hea_schedule_ui.json");
            InputStream hea_schedule_data =
                servletContext.getResourceAsStream("/WEB-INF/resources/hea_schedule_data.json");

            ObjectMapper mapper = new ObjectMapper();

            Raw raw = new Raw();
            try {
                String data = readFromInputStream(hea_schedule_data);
                data = data.replace("__TITLE__", LocalDate.parse(date).format(df));
                JsonNode jsonData = mapper.readTree(data);
                ArrayNode listItems = (ArrayNode) jsonData.findValue("listItems");
                for (String slot : sch) {
                    ObjectNode itemNode = mapper.createObjectNode();
                    itemNode.put("listItemIdentifier", slot);
                    itemNode.put("token", slot);
                    ObjectNode textContentNode = mapper.createObjectNode();
                    ObjectNode primaryText = mapper.createObjectNode();
                    primaryText.put("type", "PlainText");
                    primaryText.put("text", slot);
                    textContentNode.replace("primaryText", primaryText);
                    ObjectNode secondaryText = mapper.createObjectNode();
                    secondaryText.put("type", "PlainText");
                    secondaryText.put("text", slot);
                    textContentNode.replace("secondaryText", secondaryText);
                    itemNode.replace("textContent", textContentNode);
                    listItems.add(itemNode);
                }
                raw.setAlexaAPL(readFromInputStream(hea_schedule_ui), jsonData.toString());
            } catch (IOException e) {
            }
            response.reply(request, raw);
        }
        response.setTransition(true);
        LOGGER.info("hea scdedule query start.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/hea_signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String hea_signup(String requestJson) {
        LOGGER.info("hea signup start.");
        Request request = initFromRequest(requestJson);
        Object param = request.getProperties(PARAM_HEA_SLOT);
        String slot = "";
        if (param != null && !param.toString()
                                   .trim()
                                   .isEmpty()) {
            slot = param.toString();
            LOGGER.info("scheduling for: " + slot);

        } else {
            LOGGER.info("No param found, will default: ");
            LOGGER.info("param: " + request.getProperties("param_hea_slot"));
            LOGGER.info("param original: " + request.getProperties(PARAM_HEA_SLOT));

            LOGGER.info("argument: " + request.getVariable("param_hea_slot"));
            LOGGER.info("argument original: " + request.getVariable(PARAM_HEA_SLOT));
        }


        return _hea_signup(request, slot);
    }

    private String _hea_signup(Request request, String slot) {
        LOGGER.info("hea signup internal  start.");

        Response response = new Response(request);
        OFSCScheduler ofsc = OFSCScheduler.getInstance();
        String date = ofsc.getAvailableDate();
        String sdate = LocalDate.parse(date).format(df);
        List<String> sch = ofsc.getAvailableSchedules(date);

        if (!sch.contains(slot)) {
            LOGGER.info("Requested SLOT " + slot + " not found, using first availble slot");
            slot = sch.get(0);
        }

        boolean flag = ofsc.bookAppointment(date, slot);

        String text = flag ? String.format(rb.getString("hea.done"), slot, sdate) : rb.getString("hea.err");
        response.reply(request, text, new String[] { rb.getString("nudges.save") });
        if (isAlexaCaller(request)) {
            replyBasicCard(request, response, flag ? rb.getString("hea.title") : "Home Energy Audit",
                           flag ? slot + " on " + date : rb.getString("hea.err"),
                           flag ? rb.getString("hea.remind") : rb.getString("hea.err2"));

        }
        response.setTransition(true);
        LOGGER.info("hea sighnup internal . end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tstat_set_heating2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tstat_set_heating2(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info("thermostat_heating2 start.");

        int temp = get_setpoint2(request);

        LOGGER.info("setpoint is resolved to " + temp);

        NestInvoker nest = NestInvoker.getInstance();

        int currtemp = Integer.parseInt(nest.readTargetTemprature());
        int target = (temp < 20) ? currtemp - temp : temp;


        Response response = set_setpoint(request, nest, target);
        response.setTransition(true);
        LOGGER.info("thermostat_heating2 stop.");

        return (responseAsString(response));
    }

    @POST
    @Path("services/tstat_set_cooling2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tstat_set_cooling2(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info("tstat_set_cooling2 start.");

        int temp = get_setpoint2(request);

        LOGGER.info("setpoint is resolved to " + temp);

        NestInvoker nest = NestInvoker.getInstance();

        int currtemp = Integer.parseInt(nest.readTargetTemprature());
        int target = (temp < 20) ? currtemp - temp : temp;


        Response response = set_setpoint(request, nest, target);
        response.setTransition(true);
        LOGGER.info("tstat_set_cooling2 stop.");

        return (responseAsString(response));
    }

    private int get_setpoint2(Request request) {
        LOGGER.info("thermostat_set2 start.");

        int temp = 2;
        Object param = request.getProperties(PARAM_TEMP);

        if (param != null && !param.toString()
                                   .trim()
                                   .isEmpty()) {
            LOGGER.info("temp is " + param.getClass().getName());
            temp = ((Double) param).intValue();
            //temp = ((Map<String, Double>) param).get("amount").intValue();
            LOGGER.info(" param found:" + temp);

        } else {
            LOGGER.info("No param found, will default to 2");
        }

        return temp;
    }

    public Response set_setpoint(Request request, NestInvoker nest, int target) {

        String text = rb.getString("tstat.err");
        try {
            String nestresponse = nest.setTargetTemprature(String.valueOf(target));

            LOGGER.info("Nest Response = " + nestresponse);

            text = String.format(rb.getString("tstat.text"), target);
        } catch (Exception e) {
            LOGGER.error("error setting the thermostat", e);
        }
        Response response = new Response(request);
        response.reply(request, text, new String[] { rb.getString("nudges.save") });

        return response;
    }

    @POST
    @Path("services/tips")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tips(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info("tips start.");
        Response response = new Response(request);
        response.reply(request, rb.getString("tips.text"));
        if (isAlexaCaller(request)) {
            InputStream hea_schedule_ui = servletContext.getResourceAsStream("/WEB-INF/resources/tips_ui.json");
            InputStream hea_schedule_data = servletContext.getResourceAsStream("/WEB-INF/resources/tips_data.json");

            ObjectMapper mapper = new ObjectMapper();

            Raw raw = new Raw();
            try {
                String data = readFromInputStream(hea_schedule_data);
                data = data.replace("__TITLE__", rb.getString("tips.title"));
                JsonNode jsonData = mapper.readTree(data);
                ArrayNode listItems = (ArrayNode) jsonData.findValue("listItems");
                {
                    ObjectNode itemNode = mapper.createObjectNode();
                    itemNode.put("listItemIdentifier", rb.getString("tips.peak.title"));
                    itemNode.put("token", rb.getString("tips.peak.title"));
                    ObjectNode textContentNode = mapper.createObjectNode();
                    ObjectNode primaryText = mapper.createObjectNode();
                    primaryText.put("type", "PlainText");
                    primaryText.put("text", rb.getString("tips.peak.title"));
                    textContentNode.replace("primaryText", primaryText);
                    ObjectNode secondaryText = mapper.createObjectNode();
                    secondaryText.put("type", "PlainText");
                    secondaryText.put("text", rb.getString("tips.peak.save"));
                    textContentNode.replace("secondaryText", secondaryText);
                    itemNode.replace("textContent", textContentNode);
                    itemNode.put("image", "https://www.dropbox.com/s/bybqccw01mhpdm2/GA_Tip_Peak_Time.png?dl=1");
                    listItems.add(itemNode);
                }
                {
                    ObjectNode itemNode = mapper.createObjectNode();
                    itemNode.put("listItemIdentifier", rb.getString("tips.hea.title"));
                    itemNode.put("token", rb.getString("tips.hea.title"));
                    ObjectNode textContentNode = mapper.createObjectNode();
                    ObjectNode primaryText = mapper.createObjectNode();
                    primaryText.put("type", "PlainText");
                    primaryText.put("text", rb.getString("tips.hea.title"));
                    textContentNode.replace("primaryText", primaryText);
                    ObjectNode secondaryText = mapper.createObjectNode();
                    secondaryText.put("type", "PlainText");
                    secondaryText.put("text", rb.getString("tips.hea.save"));
                    textContentNode.replace("secondaryText", secondaryText);
                    itemNode.replace("textContent", textContentNode);
                    itemNode.put("image",
                                 "https://www.dropbox.com/s/toorsfxxshorewo/GA_Tip_home_energy_audit.png?dl=1");
                    listItems.add(itemNode);
                }
                {
                    String title = rb.getString(SUMMER ? "tips.ststat.title" : "tips.wtstat.title");
                    ObjectNode itemNode = mapper.createObjectNode();
                    itemNode.put("listItemIdentifier", title);
                    itemNode.put("token", title);
                    ObjectNode textContentNode = mapper.createObjectNode();
                    ObjectNode primaryText = mapper.createObjectNode();
                    primaryText.put("type", "PlainText");
                    primaryText.put("text", title);
                    textContentNode.replace("primaryText", primaryText);
                    ObjectNode secondaryText = mapper.createObjectNode();
                    secondaryText.put("type", "PlainText");
                    secondaryText.put("text", rb.getString(SUMMER ? "tips.ststat.save" : "tips.wtstat.save"));
                    textContentNode.replace("secondaryText", secondaryText);
                    itemNode.replace("textContent", textContentNode);
                    itemNode.put("image",
                                 "https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png");
                    listItems.add(itemNode);
                }
                raw.setAlexaAPL(readFromInputStream(hea_schedule_ui), jsonData.toString());
            } catch (IOException e) {
            }
            
            response.reply(request, raw);
        }
        response.setKeepTurn(true);
        response.setTransition(true);
        LOGGER.info("tips end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tip_tstat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tip_tstat(String requestJson) {

        Request request = initFromRequest(requestJson);
        return SUMMER ? tstat_tip_summer(request) : tstat_tip_winter(request);
    }


    private String tstat_tip_summer(Request request) {
        LOGGER.info("tstat_tip_summer start.");

        Response response = new Response(request);


        int curTemp = Integer.parseInt(NestInvoker.getInstance().readTargetTemprature());

        String tip = String.format(rb.getString("tip.ststat.current"), curTemp);
        String suggestions[] = new String[] { rb.getString("tip.ststat.raise"), rb.getString("nudges.save") };
        if (curTemp < MAX_TEMP) {
            tip += rb.getString("tip.ststat.explain");
        } else {
            tip += rb.getString("tip.ststat.already");
            suggestions = new String[] { rb.getString("nudges.save") };
        }
        response.reply(request, tip);
        tip = String.format(rb.getString("tip.ststat.current2"), curTemp);
        if (curTemp >= MAX_TEMP)
            tip = String.format(rb.getString("tip.ststat.already2"), curTemp);
        if (isAlexaCaller(request)) {
            replyBasicCardImage(request, response, rb.getString("tip.ststat.title"), tip,
                                "https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png");
        }
        response.setTransition(true);
        LOGGER.info("tstat_tip_summer end.");
        return (responseAsString(response));
    }

    private String tstat_tip_winter(Request request) {
        LOGGER.info("tstat_tip_winter start.");

        Response response = new Response(request);

        int curTemp = Integer.parseInt(NestInvoker.getInstance().readTargetTemprature());

        String tip = String.format(rb.getString("tip.wtstat.current"), curTemp);
        String suggestions[] = new String[] { rb.getString("tip.wtstat.lower"), rb.getString("nudges.save") };
        if (curTemp > MIN_TEMP) {
            tip += rb.getString("tip.wtstat.explain");
            ;
        } else {
            tip += rb.getString("tip.ststat.already");
            suggestions = new String[] { rb.getString("nudges.save") };
        }
        response.reply(request, tip);

        tip = String.format(rb.getString("tip.wtstat.current2"), curTemp);
        if (curTemp <= MIN_TEMP)
            tip = String.format(rb.getString("tip.wtstat.already2"), curTemp);
        if (isAlexaCaller(request)) {
            replyBasicCardImage(request, response, rb.getString("tip.ststat.title"), tip,
                                "https://s3.amazonaws.com/cbalane-ihd-demo/assets/Tips_UTILITYCO_181220_tip092_set_thermostat_wisely_summer.png");
        }
        response.setTransition(true);
        LOGGER.info("tstat_tip_winter end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tip_ev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tip_ev(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info(" ev_tip start.");
        Response response = new Response(request);
        response.reply(request, rb.getString("tip.ev.text"), new String[] { rb.getString("nudges.save") });
        response.setTransition(true);
        LOGGER.info(" ev_tip end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tip_hea")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tip_hea(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info(" tip_hea start.");
        Response response = new Response(request);
        response.reply(request, rb.getString("tip.hea.text"),
                       new String[] { rb.getString("nudges.save"), rb.getString("nudges.audit") });
        response.setTransition(true);
        LOGGER.info(" tip_hea end.");
        return (responseAsString(response));
    }

    @POST
    @Path("services/tip_peak")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tip_peak(String requestJson) {

        Request request = initFromRequest(requestJson);
        LOGGER.info(" tip_peak start.");
        Response response = new Response(request);
        response.reply(request, rb.getString("tip.peak.text"),
                       new String[] { rb.getString("nudges.save"), rb.getString("nudges.peak") });
        response.setTransition(true);
        LOGGER.info(" tip_peak end.");
        return (responseAsString(response));
    }
    // Provide list of services available for Oracle Digital Assistant

    @GET
    @Path("services")
    @Produces(MediaType.APPLICATION_JSON)
    public String getServices() {

        // Provide method implementation.
        // TODO
        String components =
            "{\"version\":\"1.1\",\"components\":[{\"name\":\"welcome\",\"properties\":{\"locale\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"upcoming_bills\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"energy_bill\",\"properties\":" +
            ENERGY_BILL_PROPERTIES + ",\"supportedActions\":" + SupportedAction.list() +
            "},{\"name\":\"bill_compare\",\"properties\":" + BILL_COMPARE_PROPERTIES + ",\"supportedActions\":" +
            SupportedAction.list() + "},{\"name\":\"bill_pay\",\"properties\":" + BILL_PAY_PROPERTIES + ",\"supportedActions\":" + SupportedAction.list() +
            "},{\"name\":\"autopay_signup\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"highbill_signup\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"peak_signup\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"hea_schedule_query\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"hea_schedule_signup\",\"properties\":{\"param_hea_slot.original\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"tstat_set_cooling\",\"properties\":{\"temperature\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"tstat_set_cooling2\",\"properties\":{\"temperature\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"tstat_set_heating\",\"properties\":{\"temperature\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"tstat_set_heating2\",\"properties\":{\"temperature\":{\"type\":\"string\",\"required\":true}},\"supportedActions\":[]},{\"name\":\"tips\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"tip_tstat\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"tip_peak\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"tip_ev\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"tip_hea\",\"properties\":{},\"supportedActions\":[]},{\"name\":\"list_select\",\"properties\":{\"OPTION\":{\"type\":\"string\",\"required\":false}},\"supportedActions\":[\"textReceived\",\"Sélection\"]}]}";
        return components;
    }

    private Request initFromRequest(String requestJson) {
        Request request = null;
        try {
            System.out.println(requestJson);
            request = Request.loadRequest(requestJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String locale = request.getVariable("profile.locale").asText();
        locale = locale.replace("-", "_");
        System.out.println("locale: " + locale);
        rb = getRB(LocaleUtils.toLocale(locale));
        return request;
    }

    private String responseAsString(Response response) {
        ObjectMapper Obj = new ObjectMapper();
        String responseJson = "";
        try {
            responseJson = Obj.writeValueAsString(response);
            System.out.println(responseJson);
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
        return (responseJson);
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private void replyBasicCard(Request request, Response response, String title, String subtile, String information) {
        InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/basic_card_ui.json");
        InputStream welcome_data = servletContext.getResourceAsStream("/WEB-INF/resources/basic_card_data.json");

        Raw raw = new Raw();
        try {
            String data = readFromInputStream(welcome_data);
            data = data.replace("__TITLE__", title).replace("__SUBTITLE__", subtile);
            data = data.replace("__TEXT__", information);
            raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
        } catch (IOException e) {
        }
        response.reply(request, raw);
    }

    private void replyBasicCardImage(Request request, Response response, String title, String information,
                                     String imageUrl) {
        InputStream welcome_ui = servletContext.getResourceAsStream("/WEB-INF/resources/basic_card_image_ui.json");
        InputStream welcome_data = servletContext.getResourceAsStream("/WEB-INF/resources/basic_card_image_data.json");

        Raw raw = new Raw();
        try {
            String data = readFromInputStream(welcome_data);
            data = data.replace("__TITLE__", title);
            data = data.replace("__TEXT__", information);
            data = data.replace("__IMAHE__", imageUrl);
            raw.setAlexaAPL(readFromInputStream(welcome_ui), data);
        } catch (IOException e) {
        }
        response.reply(request, raw);
    }

    private boolean isAlexaCaller(Request request) {
        return "alexa"
               .equalsIgnoreCase((request.getVariable("profile.clientType") != null ?
                                  request.getVariable("profile.clientType").asText() : ""));
    }
}
