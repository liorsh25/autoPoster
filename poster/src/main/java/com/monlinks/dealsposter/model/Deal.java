package com.monlinks.dealsposter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deals")
public class Deal{
	
	@Id
	private String extId;
	private Merchant merchant;
	private String description;
	private String code;
	private String discount;
	private String title;
	private String dealImageUrl;
	private String postedDate;
	private String startDate;
	private String endDate;
	private String affUrl;
	private String collectDate;
	private String dealSource;// = icodes-us , viglink, groupon
	private String category;
	private String posterId;

	public Deal(String extId, Merchant merchant, String description, String code, String discount, String dealImageUrl,
			String postedDate, String startDate, String endDate,String title,String affUrl,String collectDate,String dealSource,String category,String posterId) {
		super();
		this.extId = extId;
		this.merchant = merchant;
		this.description = description;
		this.code = code;
		this.discount = discount;
		this.dealImageUrl = dealImageUrl;
		this.postedDate = postedDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.title = title;
		this.affUrl = affUrl;
		this.collectDate = collectDate;
		this.dealSource = dealSource;
		this.setCategory(category);
		this.posterId = posterId;
	}

//	public Deal(String extId2, Merchant merchant2, String description2, String code2, String discount2,	String dealImageUrl2, String startDate2, String endDate2) {
//		super();
//		this.extId = extId2;
//		this.merchant = merchant2;
//		this.description = description2;
//		this.code = code2;
//		this.discount = discount2;
//		this.dealImageUrl = dealImageUrl2;
//		this.postedDate = null;
//		this.startDate = startDate2;
//		this.endDate = endDate2;
//	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}

	public String getDealImageUrl() {
		return dealImageUrl;
	}

	public void setDealImageUrl(String dealImageUrl) {
		this.dealImageUrl = dealImageUrl;
	}

	public String getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(String postedDate) {
		this.postedDate = postedDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAffUrl() {
		return affUrl;
	}

	public void setAffUrl(String affUrl) {
		this.affUrl = affUrl;
	}

	
	public String getCollectDate() {
		return collectDate;
	}

	public void setCollectDate(String collectDate) {
		this.collectDate = collectDate;
	}

	public String getDealSource() {
		return dealSource;
	}

	public void setDealSource(String dealSource) {
		this.dealSource = dealSource;
	}



	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	

	public String getPosterId() {
		return posterId;
	}

	public void setPosterId(String posterId) {
		this.posterId = posterId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Deal [extId=");
		builder.append(extId);
		builder.append(", merchant=");
		builder.append(merchant);
		builder.append(", description=");
		builder.append(description);
		builder.append(", code=");
		builder.append(code);
		builder.append(", discount=");
		builder.append(discount);
		builder.append(", title=");
		builder.append(title);
		builder.append(", dealImageUrl=");
		builder.append(dealImageUrl);
		builder.append(", postedDate=");
		builder.append(postedDate);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", affUrl=");
		builder.append(affUrl);
		builder.append(", collectDate=");
		builder.append(collectDate);
		builder.append(", dealSource=");
		builder.append(dealSource);
		builder.append(", category=");
		builder.append(category);
		builder.append(", posterId=");
		builder.append(posterId);
		builder.append("]");
		return builder.toString();
	}
	
	
}
