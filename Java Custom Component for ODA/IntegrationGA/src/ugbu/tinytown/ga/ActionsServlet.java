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

import java.io.IOException;

import java.text.NumberFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.actions.api.App;

import ugbu.dss.demo.c2mapi.CustomerServiceStatus;
import ugbu.dss.demo.c2mapi.CustomerServiceStatusResults;
import ugbu.dss.demo.c2mapi.RebateStatusInvoker;

import ugbu.tinytown.alexa.AlexaGenericResource;

/**
 * Handles request received via HTTP POST and delegates it to your Actions app. See: [Request
 * handling in Google App
 * Engine](https://cloud.google.com/appengine/docs/standard/java/how-requests-are-handled).
 */
@WebServlet(name = "actions", value = "/")
public class ActionsServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(UtilcoGAWebHookApp.class);
    private final App actionsApp = new UtilcoGAWebHookApp();

    private static DNP dnp_status = DNP.CLEAR;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        LOG.info("doPost, body = {}", body);

        try {
            String jsonResponse = actionsApp.handleRequest(body, getHeadersMap(req)).get();
            LOG.info("Generated json = {}", jsonResponse);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            writeResponse(res, jsonResponse);
        } catch (InterruptedException e) {
            handleError(res, e);
        } catch (ExecutionException e) {
            handleError(res, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String dnp = request.getParameter("dnp");
        String mock = request.getParameter("mock");

        response.setContentType("text/plain");
        if (dnp == null || dnp.trim().length() == 0) {

            response.getWriter().println((mock != null && mock.trim().length() > 0) ? getMockStatus(mock) :
                                         getStatus(request.getParameter("acct")));
            return;
        }

        // serve the image

        String status = "DNP before disconnect";

        if (dnp.equalsIgnoreCase("clear")) {
            dnp_status = DNP.CLEAR;
            status = "DNP cleared";
        }

        if (dnp.equalsIgnoreCase("before")) {
            dnp_status = DNP.BEFORE;
            status = "DNP before payment";
        }

        if (dnp.equalsIgnoreCase("during")) {
            dnp_status = DNP.DURING;
            status = "DNP during disconnect";
        }

        response.getWriter().println(status);

    }

    private void writeResponse(HttpServletResponse res, String asJson) {
        try {
            res.getWriter().write(asJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleError(HttpServletResponse res, Throwable throwable) {
        try {
            throwable.printStackTrace();
            LOG.error("Error in App.handleRequest ", throwable);
            res.getWriter().write("Error handling the intent - " + throwable.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            LOG.info("Adding [" + key + ":" + value + "]");
            map.put(key, value);
        }

        return map;
    }


    public static String getStatus(String acct) {
        CustomerServiceStatus loader = CustomerServiceStatus.getInstance();
        CustomerServiceStatusResults results = loader.invoke(acct, "");
        int enroll = RebateStatusInvoker.getInstance().invoke(UtilcoGAWebHookApp.ACCT_ID, "", "");

        ResourceBundle rb = UtilcoGAWebHookApp.rb == null ? AlexaGenericResource.rb : UtilcoGAWebHookApp.rb;
        NumberFormat cf = UtilcoGAWebHookApp.cf == null ? AlexaGenericResource.cf : UtilcoGAWebHookApp.cf;
        DateTimeFormatter df = UtilcoGAWebHookApp.df == null ? AlexaGenericResource.df : UtilcoGAWebHookApp.df;;

        if (results != null) {
            if (results.elec_outage) {
                String status = rb.getString("status.outage");

                if (results.restore != null) {
                    status += String.format(rb.getString("status.restore"), results.restore);
                }

                return status;
            }

            if (results.NP_amt > 0 || dnp_status != DNP.CLEAR) {

                if (dnp_status != DNP.CLEAR) {
                    results.NP_amt = 78.38f;
                    results.NP_date =
                        (dnp_status == DNP.BEFORE) ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(1);
                }
                if (results.NP_date.isBefore(LocalDate.now())) {
                    return String.format(rb.getString("status.dnp_after"), cf.format(results.NP_amt));
                }

                else if (results.NP_date.isAfter(LocalDate.now()))
                    return String.format(rb.getString("status.dnp_before"), cf.format(results.NP_amt),
                                         results.NP_date.format(df));
            }

            if (results.hasGasLeak())
                return rb.getString("status.gasleak");

            if (results.hasWaterLeak())
                return rb.getString("status.waterleak");
        }

        if (enroll > 0)
            return rb.getString("status.peakday");
        return "Normal";

    }

    public static String getMockStatus(String acct) {

        if (acct != null) {
            if (acct.equalsIgnoreCase("acct1"))
                return "Your account has past due balance of $$65.32. Please pay before Saturday, April 5th to avoid any disruption to your service.";

            if (acct.equalsIgnoreCase("acct2"))
                return "Your service has been disconnected. Please pay $65.32 at earliest to reconnect your service.";

            if (acct.equalsIgnoreCase("acct3"))
                return "We are working on the outage in your area. Power is expected to be restored in 45 mins. ";

            if (acct.equalsIgnoreCase("acct4"))
                return "We have noticed an unusual sustained gas consumption on your account. This could indicate gas leak at your property. Please contact your plumber to take appropriate action.";

            if (acct.equalsIgnoreCase("acct5"))
                return "There might potentially be an water leak at your property. Please contact your plumber as soon as possible.";

            if (acct.equalsIgnoreCase("acct6"))
                return "Tommorow is a Peak Rewards Day. Reduce your energy consumption from 2:00pm to 7:00pm to earn rewards.";
        }
        return "ActionsServlet is listening but requires valid POST request to respond with Action response.";

    }

    enum DNP {
        CLEAR,
        BEFORE,
        DURING;
    }
}
