package com.monlinks.dealsposter.dealscollectorservice.dealsfeeds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.XML;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monlinks.dealsposter.dealscollectorservice.DealsCollectorJob;
import com.monlinks.dealsposter.model.CategoriesEnums;
import com.monlinks.dealsposter.model.CollectSource;
import com.monlinks.dealsposter.model.CollectType;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.Merchant;
import com.monlinks.dealsposter.model.MerchantLocation;
import com.monlinks.dealsposter.utils.ArrayUtils;

public class ICodesDealsFetcher extends AbsDealsFetcher {
	private static Logger log = LogManager.getLogger(ICodesDealsFetcher.class.getName());
	
	private static Map<MerchantLocation,String> urlPrefixMapping = new HashMap<>();
	//String dealFetchUrlTemplate = "http://webservices.icodes-us.com/ws2_us.php?UserName=pagerank10&SubscriptionID=f224e9f969fdca81f94f18bf710e464b&RequestType=Codes&Action=Merchant&Query="+ website +"&Sort=id&PageSize=50";
	//http://webservices.icodes.co.uk/ws2.php
	String dealFetchUrlTemplate = "%s&RequestType=%s&Action=%s&Query=%s&Sort=id&PageSize=50";
	private static Map<String,String> categoryNamesMap = new HashMap<>();
			
	
	
	public ICodesDealsFetcher() {
		super();
		urlPrefixMapping.put(MerchantLocation.UK, "http://webservices.icodes.co.uk/ws2.php?UserName=icodes&SubscriptionID=f40d44914835ea77ea1d1b6f053d0424");
		urlPrefixMapping.put(MerchantLocation.US, "http://webservices.icodes-us.com/ws2_us.php?UserName=pagerank10&SubscriptionID=f224e9f969fdca81f94f18bf710e464b");
		
		categoryNamesMap.put(CategoriesEnums.FASHION.name()+"_"+MerchantLocation.US.name(), "Apparel");
		categoryNamesMap.put(CategoriesEnums.FASHION.name()+"_"+MerchantLocation.UK.name(), "Clothing%20and%20Footwear");
		categoryNamesMap.put(CategoriesEnums.TRAVEL.name()+"_"+MerchantLocation.UK.name(), "Travel");
		categoryNamesMap.put(CategoriesEnums.TRAVEL.name()+"_"+MerchantLocation.US.name(), "Travel_and_Vacations");
	}


	
	
	@Override
	protected String buildFetchOffersUrl(String merchantName,MerchantLocation merchantLocation) {
		String formatedUrl = String.format(dealFetchUrlTemplate,urlPrefixMapping.get(merchantLocation) ,"Offers","Merchant",URLEncoder.encode(merchantName));
		return formatedUrl;
	}
	
	
	@Override
	protected String buildFetchCodesUrl(String merchantName,MerchantLocation merchantLocation) {
		String formatedUrl = String.format(dealFetchUrlTemplate,urlPrefixMapping.get(merchantLocation), "Codes","Merchant",URLEncoder.encode(merchantName));
		return formatedUrl;
	}


	@Override
	protected String buildFetchCodesUrlForCategory(String category,MerchantLocation location) {
		String formatedUrl = String.format(dealFetchUrlTemplate,urlPrefixMapping.get(location), "Codes","Category",categoryNamesMap.get(category+"_"+location));
		return formatedUrl;
	}
	
	@Override
	protected String buildFetchOffersUrlForCategory(String category,MerchantLocation location) {
		String formatedUrl = String.format(dealFetchUrlTemplate,urlPrefixMapping.get(location), "Offers","Category",categoryNamesMap.get(category+"_"+location));
		return formatedUrl;
	}




	@Override
	public List<Deal> fetchDealsByCategory(String categoryName, MerchantLocation location) {
		List<Deal> retDeals;
		
		JsonArray jsonArr = null;
				
		log.debug("@@@@@@@@@ Going to fetch coupons and offers for : "+ categoryName);
		JsonArray thisCAtegoryDeals = null;
		if(location.equals(MerchantLocation.ALL)){
			JsonArray arr1 = this.fetchCodesByCategory(categoryName, MerchantLocation.US);
			JsonArray arr2 = this.fetchOffersByCategory(categoryName, MerchantLocation.US);
			JsonArray tempArr1 = ArrayUtils.calcCorrectArr(arr1,arr2);
			JsonArray arr3 = this.fetchCodesByCategory(categoryName, MerchantLocation.UK);
			JsonArray arr4 = this.fetchOffersByCategory(categoryName, MerchantLocation.UK);
			JsonArray tempArr2 = ArrayUtils.calcCorrectArr(arr3,arr4);
			thisCAtegoryDeals = ArrayUtils.calcCorrectArr(tempArr1,tempArr2);
		}else{
			JsonArray arr1 = this.fetchCodesByCategory(categoryName, location);
			JsonArray arr2 = this.fetchOffersByCategory(categoryName, location);
			thisCAtegoryDeals = ArrayUtils.calcCorrectArr(arr1,arr2);
		}
		
		int numberOfDeals = (thisCAtegoryDeals==null?0:thisCAtegoryDeals.size());
		log.debug("@@@@@@@@@ Found "+ numberOfDeals +" coupons and offers for category: "+ categoryName);
		
		jsonArr = ArrayUtils.calcCorrectArr(jsonArr,thisCAtegoryDeals);
		retDeals = convertJsonToDeals(jsonArr,location,categoryName);
		return retDeals;
	}

	@Override
	public List<Deal> fetchDealsByList(String merchantsList, MerchantLocation merchantLocation) {
		JsonArray retArr = null;
		List<Deal> retDeals= null;
		String merchantNamesListStr = merchantsList;
		String[] merchantNamesListArr = merchantNamesListStr.split(",");
		log.debug("Go over merchant list:"+ Arrays.toString(merchantNamesListArr));
				
		for (String merchantName : merchantNamesListArr) {
			log.debug("@@@@@@@@@ Going to fetch coupons and offers for : "+ merchantName);
			JsonArray thisMerchantDeals = null;
			if(merchantLocation.equals(MerchantLocation.ALL)){
				JsonArray arr1 = this.fetchCodes(merchantName, MerchantLocation.US);
				JsonArray arr2 = this.fetchOffers(merchantName, MerchantLocation.US);
				JsonArray tempArr1 = ArrayUtils.calcCorrectArr(arr1,arr2);
				JsonArray arr3 = this.fetchCodes(merchantName, MerchantLocation.UK);
				JsonArray arr4 = this.fetchOffers(merchantName, MerchantLocation.UK);
				JsonArray tempArr2 = ArrayUtils.calcCorrectArr(arr3,arr4);
				thisMerchantDeals = ArrayUtils.calcCorrectArr(tempArr1,tempArr2);
			}else{
				JsonArray arr1 = this.fetchCodes(merchantName, merchantLocation);
				JsonArray arr2 = this.fetchOffers(merchantName, merchantLocation);
				thisMerchantDeals = ArrayUtils.calcCorrectArr(arr1,arr2);
			}
			
			
			int numberOfDeals = (thisMerchantDeals==null?0:thisMerchantDeals.size());
			log.debug("@@@@@@@@@ Found "+ numberOfDeals +" coupons and offers for : "+ merchantName);
			
			retArr = ArrayUtils.calcCorrectArr(retArr,thisMerchantDeals);
		}
		
		retDeals = convertJsonToDeals(retArr,merchantLocation,null);
		return retDeals;
	}
	

	

	protected Deal createDeal(JsonObject jsonDeal, MerchantLocation location2, String categoryName)  {
		String extId = getStringFromJsonAtt(jsonDeal,"icid"); 
		String merchantName =getStringFromJsonAtt(jsonDeal,"merchant");  
		String merchantdomain =getStringFromJsonAtt(jsonDeal,"merchant_url"); 
		MerchantLocation location = location2;
		String imageUrl = null;
		Merchant merchant = new Merchant(merchantName, merchantdomain, location, imageUrl);

		String description = getStringFromJsonAtt(jsonDeal,"description"); 
		String code = getStringFromJsonAtt(jsonDeal,"voucher_code"); 
		String discount = null;
		String dealImageUrl = selectImage(jsonDeal); 
		String postedDate = null;// not posted yet
		String startDate = getStringFromJsonAtt(jsonDeal,"start_date"); //  "startDate": "2016-11-16 00:00:00",  "endDate": "2016-12-19 23:59:59",
		String endDate = getStringFromJsonAtt(jsonDeal,"expiry_date"); 
		String title = getStringFromJsonAtt(jsonDeal,"title"); 
		
		String affUrl = null;//will be filled by the publisherJob
		String collectDate =LocalDateTime.now().toString();
		String dealSource= "icodes";
				
		Deal dealToInsert = new Deal(extId, merchant, description, code, discount, dealImageUrl, postedDate, startDate,	endDate, title,affUrl, collectDate, dealSource , categoryName,null);
		return dealToInsert;
	}

	private String selectImage(JsonObject jsonDeal) {
		String retImage = null;
		retImage = getStringFromJsonAtt(jsonDeal,"img_url");
		if(retImage == null || retImage.isEmpty()){
			retImage = getForcedImage(getStringFromJsonAtt(jsonDeal,"merchant_url"));
		}
		
		log.debug("selectImage retImage="+ retImage);
		return retImage;
	}

	protected JsonArray getItemsJsonArray(JsonObject responseJson) {
		JsonArray retArr = null;
		if(responseJson.getAsJsonObject("items").get("Results").getAsString().equals("") || responseJson.getAsJsonObject("items").get("Results").getAsString().equals("0")){
			log.debug("NO Items returned from deals in response:" +responseJson );
			return retArr; 
		}
		
		try {
			//retArr = responseJson.getJSONObject("items").getJSONArray("item");
			retArr = responseJson.getAsJsonObject("items").getAsJsonArray("item");
		} catch (Exception e) {
			JsonArray jsonArray = new JsonArray();
			jsonArray.add(responseJson.getAsJsonObject("items").getAsJsonObject("item"));
			retArr = jsonArray;
		}
		
		return retArr;
	}


	public JsonArray fetchCodesByCategory(String category,MerchantLocation location) {
		String fetchCodessUrl = buildFetchCodesUrlForCategory(category,location);
		JsonArray retArr = fetchDealsArrayFromUrl(fetchCodessUrl);
		return retArr;
	}


	public JsonArray fetchOffersByCategory(String category,MerchantLocation location) {
		String fetchCodessUrl = buildFetchOffersUrlForCategory(category,location);
		JsonArray retArr = fetchDealsArrayFromUrl(fetchCodessUrl);
		return retArr;
	}




	public JsonArray fetchCodes(String merchantName,MerchantLocation merchantLocation) {
		String fetchCodessUrl = buildFetchCodesUrl(merchantName,merchantLocation);
		log.debug("Going to fetch COUPONS from:"+ fetchCodessUrl);
		JsonArray retArr = fetchDealsArrayFromUrl(fetchCodessUrl);
		return retArr;
	}


	public JsonArray fetchOffers(String merchantName,MerchantLocation merchantLocation)  {
		String fetchOffersUrl = buildFetchOffersUrl(merchantName,merchantLocation);
		log.debug("Going to fetch OFFERS from:"+ fetchOffersUrl);
		JsonArray retArr = fetchDealsArrayFromUrl(fetchOffersUrl);
		return retArr;
	}
	
	protected JsonObject getJsonObj(String fetchFromUrl) throws IOException, JSONException{
		
		//	return getDammyJson();
			
			log.debug("fetchFromUrl="+fetchFromUrl);
			URL url = new URL(fetchFromUrl);
				
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
		
	        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),Charset.forName("ISO-8859-1")));
	      
	        String inputLine;
	        StringBuilder sb = new StringBuilder();
	        while ((inputLine = in.readLine()) != null){
	        	sb.append(inputLine);
	        }
	        in.close();
		 	
	        String xmlString = sb.toString();
	        
	        String jsonString = XML.toJSONObject(xmlString).toString();
	        JsonParser jsonParser = new JsonParser();
			JsonObject xmlJSONObj = (JsonObject)jsonParser .parse(jsonString);
	        
		 return xmlJSONObj;
		}



	

}
