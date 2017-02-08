package com.monlinks.dealsposter.dealscollectorservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.monlinks.dealsposter.dao.DealsRepository;
import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.ICodesDealsFetcher;
import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.IDealsFetcher;
import com.monlinks.dealsposter.model.CollectSource;
import com.monlinks.dealsposter.model.CollectType;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.Merchant;
import com.monlinks.dealsposter.model.MerchantLocation;
import com.monlinks.dealsposter.model.PosterData;
import com.monlinks.dealsposter.utils.ArrayUtils;



public class DealsCollectorJob extends TimerTask {
	private DealsRepository repository;
	private CollectType collectType;
	private MerchantLocation merchantLocation;
	private List<PosterData> postersDataList;

	private static Logger log = LogManager.getLogger(DealsCollectorJob.class.getName());

	public DealsCollectorJob(List<PosterData> postersDataList, DealsRepository repository) {
		this.repository = repository;
//		this.collectType = collectType;
//		this.merchantLocation = merchantLocation;
		this.postersDataList = postersDataList;
		
	
		
	}

	@Override
	public void run() {
		log.debug("############### START DealsCollectorJob ########################");
		for (PosterData posterData : postersDataList) {
			handleCollect(posterData);
		}
		
	}

	private void handleCollect(PosterData posterData) {
		
		try {
						
			List<Deal>  dealsList = getJsonDeals(posterData.getCollectSource());
			if(dealsList == null || dealsList.size()==0){
				log.debug("NO deals were found, will not update DB. collectSource="+posterData);
				return;
			}
			
			
//
//			//build deals list
//			for (int i = 0; i < dealsCodes.size(); i++) {
//				JsonObject categoryDeal = (JsonObject) dealsCodes.get(i);
//				Deal dealToSave = createDeal(categoryDeal,collectSource);
//				dealsList.add(dealToSave);
//			}

			List<Deal> newSavedDeals = new ArrayList<Deal>();
			//go over each deal, only if not exist add it to the DB
			for (Deal deal : dealsList) {
				Deal foundDeal = repository.findOne(deal.getExtId());
				if(foundDeal == null){
					deal.setPosterId(posterData.getPosterId());
					repository.save(deal);
					newSavedDeals.add(deal);
					log.debug("SAVED the deal:" + deal);
				}else{
					log.debug("Deal already exist:"+ deal.getExtId());
				}
			}
			
			log.debug("FINISHED collect and save "+newSavedDeals.size()+" new deals:"+newSavedDeals);
			


		} catch (Exception e) {
			log.error("Problem in collecting deals",e);
		}
		
	}

	private List<Deal>  getJsonDeals(CollectSource collectSource) {
		List<Deal> retArr = null;
		CollectType collectType = collectSource.getCollectType();
		
		switch (collectType) {
		case CATEGORY:
			retArr = handleCollectByCategory1(collectSource);
			
			break;
		case MERCHANT_LIST:
			retArr = handleCollectByList1(collectSource);
			
			break;
		default:
			break;
		}
		
		int numberOfDeals = (retArr==null?0:retArr.size());
		log.debug("##### Found total "+ numberOfDeals +" deals.");
		
		return retArr;
	}

	
	private List<Deal>  handleCollectByCategory1(CollectSource collectSource) {
		List<Deal> collectedDeals = new ArrayList<Deal>();
		String categoryName = collectSource.getCollectValue();
		IDealsFetcher[] dealsFetchers = collectSource.getDealsFetchers();
		
		for (IDealsFetcher iDealsFetcher : dealsFetchers) {
			List<Deal> temp = iDealsFetcher.fetchDealsByCategory(categoryName, collectSource.getMerchantLocation());
			collectedDeals.addAll(temp);
		}
		return collectedDeals;
	}
	
	private List<Deal>  handleCollectByList1(CollectSource collectSource) {
		List<Deal> collectedDeals = new ArrayList<Deal>();
		
		IDealsFetcher[] dealsFetchers = collectSource.getDealsFetchers();
		String merchantsList =collectSource.getCollectValue();
		
		for (IDealsFetcher iDealsFetcher : dealsFetchers) {
			List<Deal> temp = iDealsFetcher.fetchDealsByList(merchantsList, collectSource.getMerchantLocation());
			collectedDeals.addAll(temp);
		}
		return collectedDeals;
	}
	
//	private JsonArray  handleCollectByCategory(CollectSource collectSource) {
//		JsonArray retArr = null;
//		String categoryName = collectSource.getCollectValue();
//		IDealsFetcher dealsFetcher = new ICodesDealsFetcher();
//		
//		log.debug("@@@@@@@@@ Going to fetch coupons and offers for : "+ categoryName);
//		JsonArray thisCAtegoryDeals = null;
//		if(collectSource.getMerchantLocation().equals(MerchantLocation.ALL)){
//			JsonArray arr1 = dealsFetcher.fetchCodesByCategory(categoryName, MerchantLocation.US);
//			JsonArray arr2 = dealsFetcher.fetchOffersByCategory(categoryName, MerchantLocation.US);
//			JsonArray tempArr1 = ArrayUtils.calcCorrectArr(arr1,arr2);
//			JsonArray arr3 = dealsFetcher.fetchCodesByCategory(categoryName, MerchantLocation.UK);
//			JsonArray arr4 = dealsFetcher.fetchOffersByCategory(categoryName, MerchantLocation.UK);
//			JsonArray tempArr2 = ArrayUtils.calcCorrectArr(arr3,arr4);
//			thisCAtegoryDeals = ArrayUtils.calcCorrectArr(tempArr1,tempArr2);
//		}else{
//			JsonArray arr1 = dealsFetcher.fetchCodesByCategory(categoryName, collectSource.getMerchantLocation());
//			JsonArray arr2 = dealsFetcher.fetchOffersByCategory(categoryName, collectSource.getMerchantLocation());
//			thisCAtegoryDeals = ArrayUtils.calcCorrectArr(arr1,arr2);
//		}
//		
//		int numberOfDeals = (thisCAtegoryDeals==null?0:thisCAtegoryDeals.size());
//		log.debug("@@@@@@@@@ Found "+ numberOfDeals +" coupons and offers for category: "+ categoryName);
//		
//		retArr = ArrayUtils.calcCorrectArr(retArr,thisCAtegoryDeals);
//		
//		return retArr;
//	}
	
//	private JsonArray  handleCollectByList(CollectSource collectSource) {
//		JsonArray retArr = null;
//		String merchantNamesListStr = collectSource.getCollectValue();
//		String[] merchantNamesListArr = merchantNamesListStr.split(",");
//		log.debug("Go over merchant list:"+ Arrays.toString(merchantNamesListArr));
//		IDealsFetcher dealsFetcher = new ICodesDealsFetcher();
//		
//		for (String merchantName : merchantNamesListArr) {
//			log.debug("@@@@@@@@@ Going to fetch coupons and offers for : "+ merchantName);
//			JsonArray thisMerchantDeals = null;
//			if(collectSource.getMerchantLocation().equals(MerchantLocation.ALL)){
//				JsonArray arr1 = dealsFetcher.fetchCodes(merchantName, MerchantLocation.US);
//				JsonArray arr2 = dealsFetcher.fetchOffers(merchantName, MerchantLocation.US);
//				JsonArray tempArr1 = ArrayUtils.calcCorrectArr(arr1,arr2);
//				JsonArray arr3 = dealsFetcher.fetchCodes(merchantName, MerchantLocation.UK);
//				JsonArray arr4 = dealsFetcher.fetchOffers(merchantName, MerchantLocation.UK);
//				JsonArray tempArr2 = ArrayUtils.calcCorrectArr(arr3,arr4);
//				thisMerchantDeals = ArrayUtils.calcCorrectArr(tempArr1,tempArr2);
//			}else{
//				JsonArray arr1 = dealsFetcher.fetchCodes(merchantName, collectSource.getMerchantLocation());
//				JsonArray arr2 = dealsFetcher.fetchOffers(merchantName, collectSource.getMerchantLocation());
//				thisMerchantDeals = ArrayUtils.calcCorrectArr(arr1,arr2);
//			}
//			
//			
//			int numberOfDeals = (thisMerchantDeals==null?0:thisMerchantDeals.size());
//			log.debug("@@@@@@@@@ Found "+ numberOfDeals +" coupons and offers for : "+ merchantName);
//			
//			retArr = ArrayUtils.calcCorrectArr(retArr,thisMerchantDeals);
//		}
//		
//		return retArr;
//	}


}
