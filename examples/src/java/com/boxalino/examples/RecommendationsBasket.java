/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boxalino.examples;

import Exception.BoxalinoException;
import Helper.CustomBasketContent;
import Helper.HttpContext;
import boxalino.client.SDK.BxChooseResponse;
import boxalino.client.SDK.BxClient;
import boxalino.client.SDK.BxRecommendationRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HASHIR
 */
public class RecommendationsBasket {

    public String _account = "boxalino_automated_tests";
    public String _password = "boxalino_automated_tests";
    private String domain;
    private List<String> logs;
    private String language;
    public boolean _print = true;
    private boolean isDev;
    public BxChooseResponse bxResponse = null;
    private HttpContext httpContext = null;
    private String ip="";
    private String referer="";
    private String currentUrl="";    
    private String userAgent = "";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods. Use this method if do not want to manage cookies
     *
     * @throws IOException if an I/O error occurs
     */
    public void recommendationsBasket() throws IOException {

        try {

            String account = this._account; // your account name
            String password = this._password; // your account password
            domain = ""; // your web-site domain (e.g.: www.abc.com)
            isDev = false; //are the data to be pushed dev or prod data?            
            logs = new ArrayList<>();
            //optional, just used here in example to collect logs
            boolean print = this._print;

            //Create HttpContext instance
            httpContext = new HttpContext(domain,userAgent,ip,referer,currentUrl);
            BxClient bxClient = new BxClient(account, password, domain, isDev, null, 0, null, null, null, null,httpContext, null, null);
            language = "en"; // a valid language code (e.g.: "en", "fr", "de", "it", ...)
            String choiceId = "basket"; //the recommendation choice id (standard choice ids are: "similar" => similar products on product detail page, "complementary" => complementary products on product detail page, "basket" => cross-selling recommendations on basket page, "search"=>search results, "home" => home page personalized suggestions, "category" => category page suggestions, "navigation" => navigation product listing pages suggestions)
            String itemFieldId = "id"; // the field you want to use to define the id of the product (normally id, but could also be a group id if you have a difference between group id and sku)

            ArrayList<CustomBasketContent> itemFieldIdValuesPrices = new ArrayList<CustomBasketContent>(); //the product ids and their prices that the user currently has in his basket
            CustomBasketContent customBasketContent = new CustomBasketContent();
            customBasketContent.Id = "1940";
            customBasketContent.Price = "10.80";
            itemFieldIdValuesPrices.add(customBasketContent);
            customBasketContent = new CustomBasketContent();
            customBasketContent.Id = "1234";
            customBasketContent.Price = "130.5";
            itemFieldIdValuesPrices.add(customBasketContent);

            int hitCount = 10; //a maximum number of recommended result to return in one page

            //create similar recommendations request
            BxRecommendationRequest bxRequest = new BxRecommendationRequest(language, choiceId, hitCount);

            //indicate the products the user currently has in his basket (reference of products for the recommendations)
            bxRequest.setBasketProductWithPrices(itemFieldId, itemFieldIdValuesPrices, "", "");

            //add the request
            bxClient.addRequest(bxRequest);

            //make the query to Boxalino server and get back the response for all requests
            bxResponse = bxClient.getResponse();

            int index=0;
            for (String item : bxResponse.getHitIds("", true, 0, 10, "id")) {
                logs.add(index + ": returned id " + item);
                index++;
            }
            if (print) {
                System.out.println(String.join("\n", logs));

            }

        } catch (BoxalinoException ex) {

            System.out.println(ex.getMessage());

        }
    }

}
