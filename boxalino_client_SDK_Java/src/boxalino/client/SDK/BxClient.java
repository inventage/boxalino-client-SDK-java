/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boxalino.client.SDK;

import Exception.BoxalinoException;
import Helper.HttpContext;
import com.boxalino.p13n.api.thrift.AutocompleteRequest;
import com.boxalino.p13n.api.thrift.AutocompleteRequestBundle;
import com.boxalino.p13n.api.thrift.AutocompleteResponse;
import com.boxalino.p13n.api.thrift.ChoiceInquiry;
import com.boxalino.p13n.api.thrift.ChoiceRequest;
import com.boxalino.p13n.api.thrift.ChoiceResponse;
import com.boxalino.p13n.api.thrift.P13nService.Client;
import com.boxalino.p13n.api.thrift.RequestContext;
import com.boxalino.p13n.api.thrift.UserRecord;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author HASHIR
 */
public class BxClient {

    private final String account;
    private final String password;
    private final boolean isDev;
    private String host;
    private Integer port;
    private String uri;
    private String schema;
    private String p13n_username;
    private String p13n_password;
    private final String domain;

    private ArrayList<BxAutocompleteRequest> autocompleteRequests = null;
    private ArrayList<BxAutocompleteResponse> autocompleteResponses = null;

    private ArrayList<BxRequest> chooseRequests;
    private ChoiceResponse chooseResponses = null;

    int _VISITOR_COOKIE_TIME = 31536000;
    private int _timeout = 2;
    private Map<String, ArrayList<String>> requestContextParameters;

    public String sessionId = null;
    public String profileId = null;

    private Map<String, String> requestMap;

    public BxClient(String account, String password, String domain, boolean isDev, String host, int port, String uri, String schema, String p13n_username, String p13n_password) {
        //default value start

        if (host == null || host.equals(Helper.Common.EMPTY_STRING)) {
            host = null;
        }
        if (port == 0) {
            port = 0;
        }
        if (uri == null || uri == Helper.Common.EMPTY_STRING) {
            uri = null;
        }
        if (schema == null || schema == Helper.Common.EMPTY_STRING) {
            schema = null;
        }
        if (p13n_username == null || p13n_username == Helper.Common.EMPTY_STRING) {
            p13n_username = null;
        }
        if (p13n_password == null || p13n_password == Helper.Common.EMPTY_STRING) {
            p13n_password = null;
        }
        //default value end

        this.chooseRequests = new ArrayList<>();
        this.requestMap = new HashMap<>();
        this._timeout = 2;
        this.requestContextParameters = new HashMap<>();
        this.sessionId = null;
        this.profileId = null;

        this.account = account;
        this.password = password;
        this.isDev = isDev;

        this.host = host;
        if (this.host == null) {
            this.host = "cdn.bx-cloud.com";
        }
        this.port = port;
        if (this.port == 0) {
            this.port = 443;
        }
        this.uri = uri;
        if (this.uri == null) {
            this.uri = "/p13n.web/p13n";
        }
        this.schema = schema;
        if (this.schema == null) {
            this.schema = "https";
        }
        this.p13n_username = p13n_username;
        if (this.p13n_username == null) {
            this.p13n_username = "boxalino";
        }
        this.p13n_password = p13n_password;
        if (this.p13n_password == null) {
            this.p13n_password = "tkZ8EXfzeZc6SdXZntCU";
        }
        this.domain = domain;
    }

    public void setRequestMap(Map<String, String> requestMap) {
        this.requestMap = requestMap;
    }

    public String getAccount(boolean checkDev) {
        //dfefault start            
        checkDev = true;
        //dfefault end

        if (checkDev && this.isDev) {
            return this.account + "_dev";
        }
        return this.account;
    }

    public String getUsername() {
        return this.getAccount(false);
    }

    public String getPassword() {
        return this.password;
    }

    public void setSessionAndProfile(String sessionId, String profileId) {
        this.sessionId = sessionId;
        this.profileId = profileId;
    }

    private String[] getSessionAndProfile() {
        return new HttpContext().getSessionAndProfile(this.sessionId, this.profileId, this.domain);
    }

    private UserRecord getUserRecord() {
        UserRecord userRecord = new UserRecord();
        userRecord.username = this.getAccount(true);
        return userRecord;
    }

    private Client getP13n(int timeout, boolean useCurlIfAvailable) throws UnsupportedEncodingException, TTransportException, URISyntaxException, MalformedURLException {
        //default start
        if (timeout == 0) {
            timeout = 2;
        }
        //default end
        useCurlIfAvailable = false;
        THttpClient transport = null;
        if (useCurlIfAvailable) {

        } else {
            try {
                    transport = new THttpClient(new URI(String.format("%s://%s%s", this.schema, this.host, this.uri)).toURL().toString());
            } catch (TTransportException ex) {
                throw ex;
            }
        }
        transport.setCustomHeader("Authorization", Base64.getEncoder().encodeToString((this.p13n_username + ':' + this.p13n_password).getBytes("UTF-8")));
        Client client = new Client(new TCompactProtocol(transport));
        transport.open();
        return client;
    }

    private Map<String, ArrayList<String>> getRequestContextParameters() {
        Map<String, ArrayList<String>> parameters = this.requestContextParameters;
        chooseRequests.forEach((request) -> {
            request.getRequestContextParameters().entrySet().forEach((v) -> {
                parameters.put(v.getKey(), new ArrayList(v.getValue()));
            });
        });
        return parameters;
    }

    public RequestContext getRequestContext() throws URISyntaxException {
        String[] list;
        list = this.getSessionAndProfile();
        setSessionAndProfile(list[0],list[1]);
        RequestContext requestContext = new RequestContext();
        String sessionIdd = this.sessionId;
        requestContext.parameters = new HashMap<String, List<String>>() {
            {
                put("User-Agent", new ArrayList<String>() {
                    {
                        add(new HttpContext().getUserAgent());
                    }
                });
                put("User-Host", new ArrayList<String>() {
                    {
                        add(new HttpContext().getIP());
                    }
                });
                put("User-SessionId", new ArrayList<String>() {
                    {
                        add(sessionIdd);
                    }
                });
                put("User-Referer", new ArrayList<String>() {
                    {
                        try {
                            add(new HttpContext().getReferer());
                        } catch (URISyntaxException ex) {
                            throw ex;
                        }
                    }
                });
                put("User-URL", new ArrayList<String>() {
                    {
                        add(new HttpContext().getCurrentUrl());
                    }
                });

            }
        };
        this.getRequestContextParameters().entrySet().forEach((k) -> {
            requestContext.parameters.put(k.getKey(), k.getValue());
        });
        if (this.requestMap != null && requestMap.containsKey("p13nRequestContext")) {
            Map<String, String> requestMapp = this.requestMap;
            requestContext.parameters = new HashMap<String, List<String>>() {
                {
                    put(requestMapp.get("p13nRequestContext"), new ArrayList<String>() {
                        {
                            add(requestContext.parameters.get("p13nRequestContext").toString());
                        }
                    });

                }
            };
        }
        return requestContext;
    }

    public ChoiceRequest getChoiceRequest(ArrayList<ChoiceInquiry> inquiries, RequestContext requestContext) throws URISyntaxException {
        ChoiceRequest choiceRequest = new ChoiceRequest();
        String[] list;
        list = this.getSessionAndProfile();

        choiceRequest.userRecord = this.getUserRecord();
        choiceRequest.profileId = profileId;
        choiceRequest.inquiries = inquiries;
        if (requestContext == null) {
            requestContext = this.getRequestContext();
        }
        choiceRequest.requestContext = requestContext;
        return choiceRequest;
    }

    public void addRequestContextParameter(String name, ArrayList<String> value) {

        this.requestContextParameters.put(name, value);
    }

    public void resetRequestContextParameter() {
        this.requestContextParameters = new HashMap<>();
    }

    protected Map<String, ArrayList<String>> getBasicRequestContextParameters() throws URISyntaxException {
        sessionId = this.getSessionAndProfile()[0];
        profileId = this.getSessionAndProfile()[1];
        return new HashMap<String, ArrayList<String>>() {
            {
                put("User-Agent", new ArrayList<String>() {
                    {
                        add(new HttpContext().getUserAgent());
                    }
                });
                put("User-Host", new ArrayList<String>() {
                    {
                        add(new HttpContext().getIP());
                    }
                });
                put("User-SessionId", new ArrayList<String>() {
                    {
                        add(sessionId);
                    }
                });
                put("User-Referer", new ArrayList<String>() {
                    {
                        try {
                            add(new HttpContext().getReferer());
                        } catch (URISyntaxException ex) {
                            throw ex;
                        }
                    }
                });
                put("User-URL", new ArrayList<String>() {
                    {
                        add(new HttpContext().getCurrentUrl());
                    }
                });

            }
        };

    }

    private void throwCorrectP13nException(BoxalinoException e) throws BoxalinoException {
        if (e.getMessage().indexOf("Could not connect ") <= 0) {
            throw new BoxalinoException("The connection to our server failed even before checking your credentials. This might be typically caused by 2 possible things: wrong values in host, port, schema or uri (typical value should be host=cdn.bx-cloud.com, port=443, uri =/p13n.web/p13n and schema=https, your values are : host=' . " + this.host + ", port=" + this.port + ", schema=" + this.schema + ", uri=" + this.uri + "). Another possibility, is that your server environment has a problem with ssl certificate (peer certificate cannot be authenticated with given ca certificates). Full error message= " + e.getMessage());
        }

        if (e.getMessage().indexOf("Bad protocol id in TCompact message") <= 0) {
            throw new BoxalinoException("The connection to our server has worked, but your credentials were refused. Provided credentials username=" + this.p13n_username + ", password=" + this.p13n_password + ". Full error message=" + e.getMessage());
        }

        if (e.getMessage().indexOf("choice not found") <= 0) {
            String[] parts = e.getMessage().split("choice not found", -1);
            String[] pieces = parts[1].split("at", -1);
            String choiceId = pieces[0].replace(':', ' ');
            throw new BoxalinoException("Configuration not live on account " + this.getAccount(true) + ": choice " + choiceId + " doesn't exist. NB: If you get a message indicating that the choice doesn't exist, go to http://intelligence.bx-cloud.com, log in your account and make sure that the choice id you want to use is published.");
        }

        if (e.getMessage().indexOf("Solr returned status 404") <= 0) {
            throw new BoxalinoException("Data not live on account " + this.getAccount(true) + ": index returns status 404. Please publish your data first, like in example backend_data_basic.php.");

        }

        if (e.getMessage().indexOf("undefined field") <= 0) {
            String[] parts = e.getMessage().split("undefined field", -1);
            String[] pieces = parts[1].split("at", -1);
            String field = pieces[0].replace(':', ' ');

            throw new BoxalinoException("You request in your filter or facets a non-existing field of your account " + this.getAccount(true) + ": field " + field + " doesn't exist.");
        }

        if (e.getMessage().indexOf("All choice variants are excluded") <= 0) {
            throw new BoxalinoException("You have an invalid configuration for with a choice defined, but having no defined strategies. This is a quite unusual case, please contact support@boxalino.com to get support.");
        }

        throw e;
    }

    private ChoiceResponse p13nchoose(ChoiceRequest choiceRequest) throws UnsupportedEncodingException, TException, IOException, URISyntaxException {
        ChoiceResponse choiceResponse = this.getP13n(this._timeout, false).choose(choiceRequest);
        if (this.requestMap.size() > 0 && this.requestMap.get("dev_bx_disp") != null && this.requestMap.get("dev_bx_disp").equals("true")) {

            new HttpContext().responseWrite("<pre><h1>Choice Request</h1>");
            new HttpContext().responseWrite(choiceRequest.getClass().getName());
            new HttpContext().responseWrite("Inquiries" + choiceRequest.inquiries);
            new HttpContext().responseWrite("ProfileId" + choiceRequest.profileId);
            new HttpContext().responseWrite("RequestContext" + choiceRequest.requestContext);
            new HttpContext().responseWrite("UserRecord" + choiceRequest.userRecord);

            new HttpContext().responseWrite("<br><h1>Choice Response</h1>");
            new HttpContext().responseWrite(choiceRequest.getClass().getName());
            new HttpContext().responseWrite("Variants" + choiceResponse.variants);
            new HttpContext().responseWrite("</pre>");
        }
        return choiceResponse;
    }

    public void addRequest(BxRequest request) {
        request.setDefaultIndexId(this.getAccount(true));
        request.setDefaultRequestMap(this.requestMap);
        this.chooseRequests.add(request);
    }

    public void resetRequests() {
        this.chooseRequests = new ArrayList<>();
    }

    public BxRequest getRequest(int index) {
        if (this.chooseRequests.size() <= index) {
            return null;
        }
        return this.chooseRequests.get(index);
    }

    public BxRequest getChoiceIdRecommendationRequest(String choiceId) {
        for (BxRequest request : chooseRequests) {
            if (request.getChoiceId() == choiceId) {
                return request;
            }
        }
        return null;
    }

    public ArrayList<BxRequest> getRecommendationRequests() {
        ArrayList<BxRequest> requests = new ArrayList<>();

        chooseRequests.stream().filter((request) -> (request instanceof BxRecommendationRequest)).forEachOrdered((request) -> {
            requests.add((BxRequest) request);
        });
        return requests;
    }

    public ChoiceRequest getThriftChoiceRequest() throws URISyntaxException {
        ArrayList<ChoiceInquiry> choiceInquiries = new ArrayList<>();

        for (BxRequest request : this.chooseRequests) {
            ChoiceInquiry choiceInquiry = new ChoiceInquiry();
            choiceInquiry.choiceId = request.getChoiceId();
            choiceInquiry.simpleSearchQuery = request.getSimpleSearchQuery();
            choiceInquiry.contextItems = request.getContextItems();
            choiceInquiry.minHitCount = (int) request.getMin();
            choiceInquiry.withRelaxation = request.getWithRelaxation();
            choiceInquiries.add(choiceInquiry);
        }

        ChoiceRequest choiceRequest = this.getChoiceRequest(choiceInquiries, this.getRequestContext());
        return choiceRequest;
    }

    protected void choose() throws TException, IOException, URISyntaxException {
        this.chooseResponses = this.p13nchoose(this.getThriftChoiceRequest());
    }

    public BxChooseResponse getResponse() throws TException, IOException, URISyntaxException {
        if (this.chooseResponses == null) {
            this.choose();
        }
        return new BxChooseResponse(this.chooseResponses, this.chooseRequests);
    }

    private void enhanceAutoCompleterequest(BxAutocompleteRequest request) {
        request.setDefaultIndexId(this.getAccount(true));
    }

    public void setAutocompleteRequests(ArrayList<BxAutocompleteRequest> requests) {
        for (BxAutocompleteRequest request : requests) {
            this.enhanceAutoCompleterequest(request);
        }
        this.autocompleteRequests = requests;
    }

    public void setAutocompleteRequest(ArrayList<BxAutocompleteRequest> request) {
        this.setAutocompleteRequests(request);
    }

    private AutocompleteResponse p13nautocomplete(AutocompleteRequest autocompleteRequest) throws UnsupportedEncodingException, TException, URISyntaxException, MalformedURLException {
        return this.getP13n(this._timeout, false).autocomplete(autocompleteRequest);
    }

    public ArrayList<AutocompleteRequest> use(ArrayList<BxAutocompleteRequest> request, String profileId, UserRecord userRecord) {
        ArrayList<AutocompleteRequest> listAutocompleteRequest = new ArrayList<>();
        request.forEach((req) -> {
            listAutocompleteRequest.add(req.getAutocompleteThriftRequest(profileId, userRecord));
        });
        return listAutocompleteRequest;
    }

    public ArrayList<BxAutocompleteResponse> use(ArrayList<BxAutocompleteResponse> response, int i) {
        BxAutocompleteRequest request = this.autocompleteRequests.get(++i);
        ArrayList<BxAutocompleteResponse> bxAutocompleteResponse = new ArrayList<>();
        for (Object req : response) {
            bxAutocompleteResponse.add(new BxAutocompleteResponse((AutocompleteResponse) req, request));
        }
        return bxAutocompleteResponse;

    }

    public ArrayList<BxAutocompleteResponse> use1(List<AutocompleteResponse> response, int i) {
        BxAutocompleteRequest request = this.autocompleteRequests.get(++i);
        ArrayList<BxAutocompleteResponse> bxAutocompleteResponse = new ArrayList<>();
        for (Object req : response) {
            bxAutocompleteResponse.add(new BxAutocompleteResponse((AutocompleteResponse) req, request));
        }
        return bxAutocompleteResponse;

    }

    private List<AutocompleteResponse> p13nautocompleteAll(ArrayList<AutocompleteRequest> requests) throws UnsupportedEncodingException, TException, URISyntaxException, MalformedURLException {
        AutocompleteRequestBundle requestBundle = new AutocompleteRequestBundle();
        requestBundle.requests = requests;
        return this.getP13n(this._timeout, false).autocompleteAll(requestBundle).responses;
    }

    public void autocomplete() throws UnsupportedEncodingException, TException, URISyntaxException, MalformedURLException {
        String[] str = this.getSessionAndProfile();
        sessionId = str[0];
        profileId = str[1];

        UserRecord userRecord = this.getUserRecord();

        ArrayList<AutocompleteRequest> p13nrequests = use(this.autocompleteRequests, profileId, userRecord);
        int i = -1;
        this.autocompleteResponses = use1(this.p13nautocompleteAll(p13nrequests), i);

    }

    public ArrayList<BxAutocompleteResponse> getAutocompleteResponses() throws UnsupportedEncodingException, TException, URISyntaxException, MalformedURLException {
        if (this.autocompleteResponses == null) {
            this.autocomplete();
        }
        return this.autocompleteResponses;
    }

    public void setTimeout(int timeout) {
        this._timeout = timeout;
    }
}
