package com.monlinks.dealsposter.dealscollectorservice.dealsfeeds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.XML;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.Merchant;
import com.monlinks.dealsposter.model.MerchantLocation;

public class GrouponDealsFetcher extends AbsDealsFetcher {
	private static Logger log = LogManager.getLogger(GrouponDealsFetcher.class.getName());
	
	String dealFetchUrlTemplate = "https://partner-int-api.groupon.com/deals.json?country_code=IL&tsToken=IL_AFF_0_202128_1023789_0&filters=topcategory:%s&offset=0&limit=20";
	
	@Override
	protected String buildFetchOffersUrl(String merchantName, MerchantLocation merchantLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String buildFetchCodesUrl(String merchantName, MerchantLocation merchantLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String buildFetchCodesUrlForCategory(String category, MerchantLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String buildFetchOffersUrlForCategory(String category, MerchantLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Deal> fetchDealsByCategory(String category, MerchantLocation location) {
		String formatedUrl = String.format(dealFetchUrlTemplate, category);
		JsonArray grouponsDeals = fetchDealsArrayFromUrl(formatedUrl);
		List<Deal> retDeals = convertJsonToDeals(grouponsDeals,location,category);
		return retDeals;
	}
	
	protected Deal createDeal(JsonObject jsonDeal, MerchantLocation location2, String categoryName)  {
		String extId = getStringFromJsonAtt(jsonDeal,"uuid"); 
		String merchantName = "גרופון";
		String merchantdomain = "http://groupon.co.il"; 
		MerchantLocation location = location2;
		String imageUrl = null;
		Merchant merchant = new Merchant(merchantName, merchantdomain, location, imageUrl);

		String description = getStringFromJsonAtt(jsonDeal,"title"); 
		String code = null;//No codes in groupon
		String discount = null;
		String dealImageUrl = selectImage(jsonDeal); 
		String postedDate = null;// not posted yet
		String startDate = cleanDate(getStringFromJsonAtt(jsonDeal,"startAt"));// endAt: "2017-03-30T20:59:59Z", startAt: "2017-01-11T22:00:00Z",
		String endDate = cleanDate(getStringFromJsonAtt(jsonDeal,"endAt")); 
		String title = getStringFromJsonAtt(jsonDeal,"shortAnnouncementTitle"); 
		
		String affUrl = getStringFromJsonAtt(jsonDeal,"dealUrl");
		String collectDate =LocalDateTime.now().toString();
		String dealSource= "groupon.co.il";
				
		Deal dealToInsert = new Deal(extId, merchant, description, code, discount, dealImageUrl, postedDate, startDate,	endDate, title,affUrl, collectDate, dealSource , categoryName , null);
		return dealToInsert;
	}
	
	private String cleanDate(String stringFromJsonAtt) {
		String retCleanDate = stringFromJsonAtt.replaceAll("T"," ");//remove the T
		retCleanDate = retCleanDate.replaceAll("Z","");//remove the Z
		return retCleanDate;
	}

	private String selectImage(JsonObject jsonDeal) {
		String retImage = null;
		retImage = getStringFromJsonAtt(jsonDeal,"largeImageUrl");
		log.debug("selectImage retImage="+ retImage);
		return retImage;
	}

	@Override
	protected JsonObject getJsonObj(String fetchFromUrl) throws MalformedURLException, IOException, JSONException {
		log.debug("fetchFromUrl="+fetchFromUrl);
		URL url = new URL(fetchFromUrl);
			
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
	
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),Charset.forName("UTF-8")));
      
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
        	sb.append(inputLine);
        }
        in.close();
	 	
        String jsonString = sb.toString();
       
        JsonParser jsonParser = new JsonParser();
		JsonObject xmlJSONObj = (JsonObject)jsonParser .parse(jsonString);
        
	 return xmlJSONObj;
	}

	@Override
	protected JsonArray getItemsJsonArray(JsonObject responseJson) {
		JsonArray retArr = null;
		if(responseJson.getAsJsonArray("deals") == null){
			log.debug("NO Items returned from deals in response:" +responseJson );
			return retArr; 
		}

		retArr = responseJson.getAsJsonArray("deals");
		return retArr;
	}

	@Override
	public List<Deal> fetchDealsByList(String merchantsList, MerchantLocation merchantLocation) {
		log.error("NOT IMPLEMENTED");
		return null;
	}


}
