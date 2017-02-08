package com.monlinks.dealsposter.model;

import org.springframework.beans.factory.annotation.Autowired;

import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.IDealsFetcher;

public class CollectSource {
	CollectType collectType;
	String collectValue;//category name or merchant list (with ,)
	MerchantLocation merchantLocation;
	IDealsFetcher[] dealsFetchers;
	
	
	public CollectSource(CollectType collectType, String collectValue, MerchantLocation merchantLocation,IDealsFetcher[] iDealsFetchers) {
		super();
		this.collectType = collectType;
		this.collectValue = collectValue;
		this.merchantLocation = merchantLocation;
		this.dealsFetchers = iDealsFetchers;
	}

	public CollectType getCollectType() {
		return collectType;
	}

	public void setCollectType(CollectType collectType) {
		this.collectType = collectType;
	}

	public String getCollectValue() {
		return collectValue;
	}

	public void setCollectValue(String collectValue) {
		this.collectValue = collectValue;
	}

	public MerchantLocation getMerchantLocation() {
		return merchantLocation;
	}

	public void setMerchantLocation(MerchantLocation merchantLocation) {
		this.merchantLocation = merchantLocation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CollectSource [collectType=");
		builder.append(collectType);
		builder.append(", collectValue=");
		builder.append(collectValue);
		builder.append(", merchantLocation=");
		builder.append(merchantLocation);
		builder.append("]");
		return builder.toString();
	}

	public IDealsFetcher[] getDealsFetchers() {
		return dealsFetchers;
	}

	public void setDealsFetchers(IDealsFetcher[] dealsFetchers) {
		this.dealsFetchers = dealsFetchers;
	}


	
	
}
