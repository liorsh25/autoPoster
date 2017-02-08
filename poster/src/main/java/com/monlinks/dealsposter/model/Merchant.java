package com.monlinks.dealsposter.model;

import org.springframework.data.mongodb.core.mapping.Document;

public class Merchant {

	private String name = "";
	private String homePageUrl = "";
//	public static final String UK_LOCATION = "uk";
//	public static final String US_LOCATION = "us";
	private MerchantLocation location = MerchantLocation.US;
	private String imageUrl=null;//image will not be added to the post. 
	

	public Merchant(String name, String homePageUrl, MerchantLocation location, String imageUrl) {
		super();
		this.name = name;
		this.homePageUrl = homePageUrl;
		this.location = location;
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public MerchantLocation getLocation() {
		return location;
	}

	public void setLocation(MerchantLocation location) {
		this.location = location;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Merchant [name=");
		builder.append(name);
		builder.append(", domain=");
		builder.append(homePageUrl);
		builder.append(", location=");
		builder.append(location);
		builder.append(", imageUrl=");
		builder.append(imageUrl);
		builder.append("]");
		return builder.toString();
	}

	public String getHomePageUrl() {
		return homePageUrl;
	}

	public void setHomePageUrl(String homePageUrl) {
		this.homePageUrl = homePageUrl;
	}


	public void setDomain(String domain) {
		this.homePageUrl = domain;
	}
	
}
