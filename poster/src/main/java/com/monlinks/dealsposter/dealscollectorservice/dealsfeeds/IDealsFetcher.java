package com.monlinks.dealsposter.dealscollectorservice.dealsfeeds;

import java.util.List;

import com.google.gson.JsonArray;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.MerchantLocation;

public interface IDealsFetcher {
//	public JsonArray fetchCodes(String merchantName,MerchantLocation merchantLocation);
//	public JsonArray fetchOffers(String merchantName,MerchantLocation merchantLocation);
//	public JsonArray fetchCodesByCategory(String category,MerchantLocation location);
//	public JsonArray fetchOffersByCategory(String category,MerchantLocation location);
	
	
	public List<Deal> fetchDealsByCategory(String category,MerchantLocation location);

	public List<Deal> fetchDealsByList(String merchantsList, MerchantLocation merchantLocation);
	
}
