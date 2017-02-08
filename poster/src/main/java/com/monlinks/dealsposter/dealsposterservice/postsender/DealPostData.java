package com.monlinks.dealsposter.dealsposterservice.postsender;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monlinks.dealsposter.dealsposterservice.linkshortener.BitlyLinkShortener;
import com.monlinks.dealsposter.dealsposterservice.linkshortener.GooLinkShortener;
import com.monlinks.dealsposter.dealsposterservice.linkshortener.LinkShortener;
import com.monlinks.dealsposter.model.Deal;

public class DealPostData implements IPostData{
	
	private static final Logger log = LoggerFactory.getLogger(DealPostData.class);

	
	private static LinkShortener linkShortener = new GooLinkShortener();
	
	
	private String startDate;
	private String endDate;
	private String title;
	private String domainName;
	private String merchantName;
	private String imgUrl = null;
    private String code;
	private String affUrl;
	private String category;
	private String description;
	
	
	
	
	public DealPostData(Deal deal) {


		try {
			if(deal != null){
				title = deal.getTitle();
				setDescription(deal.getDescription());
				domainName = extractDomain(deal.getMerchant().getHomePageUrl());
				affUrl = buildAffUrl(deal);// makeShortLink(deal.getMerchant().getHomePageUrl());//makeShortLink(buildAffUrl(deal.getMerchant().getHomePageUrl()));//jsonObj.getString("affiliate_url");
				merchantName = deal.getMerchant().getName();
				setCode(deal.getCode());
				startDate = formatDate(deal.getStartDate());
				endDate = formatDate(deal.getEndDate());
				imgUrl = deal.getDealImageUrl();
				category = deal.getCategory();

			}
		} catch (Exception e) {
			log.error("Problem creating DealPostData",e );
		}

	}

	
	public String buildAffUrl(Deal deal){
		//TODO: will call the monlinks API
		//TODO: call link affiliator of prosperent
		//currently will use default properent
//		String PROSP_URL_TEMPLATE = "http://prosperent.com/api/linkaffiliator/redirect?apiKey=%s&location=%s&url=%s";
//		String pubId = "34e313e356738fa5d0f03cd70db9a8ea";
//		return String.format(PROSP_URL_TEMPLATE, pubId ,URLEncoder.encode("http://facebook.com"), URLEncoder.encode(merchantUrl));
		
		
		
		
		if(deal.getAffUrl()!= null && !deal.getAffUrl().isEmpty()){
			//The affiliate url already exist. Don't do anything, just return the affiliate url
			return deal.getAffUrl();
		}
		
		
		String merchantUrl = deal.getMerchant().getHomePageUrl();
		
		String retUrl = merchantUrl;
		String PROSP_GET_LINK_URL_TEMPLATE = "http://prosperent.com/api/linkaffiliator/url?apiKey=%s&location=%s&url=%s";
		String pubId = "34e313e356738fa5d0f03cd70db9a8ea";
		String getLinkUrl = String.format(PROSP_GET_LINK_URL_TEMPLATE, pubId ,URLEncoder.encode("http://facebook.com"), URLEncoder.encode(merchantUrl));
		log.debug("GET LINK from:"+ getLinkUrl);
		
		String prosAffUrl = getAffUrlFromPros(getLinkUrl);
		log.debug("prosAffUrl="+ prosAffUrl);
		if(prosAffUrl != null){
			retUrl = prosAffUrl;
		}
		
		if(!retUrl.contains("prosperent")){
			log.warn("This merchant website is not affiliated by prosperent: "+ retUrl);
		}
				
		log.debug("retUrl="+ retUrl);
		return retUrl;
		
		//VIGLINK
//		String VIG_URL_TEMPLATE = "http://redirect.viglink.com?key=%s&u=%s";
//		String pubId = "10a309a920033f8b2c751717059df529";//"e705fdd58001fe5ef00dbda2d47ad71a";
//		return String.format(VIG_URL_TEMPLATE, pubId , URLEncoder.encode(merchantUrl));
		
	
	}
	
	private String getAffUrlFromPros(String getLinkUrl) {
		String retUrl = null;
		InputStream is = null;
		try {
			URL url = new URL(getLinkUrl);
			URLConnection conn = url.openConnection();
			is = conn.getInputStream();
			
			Scanner s = new Scanner(is).useDelimiter("\\A");
			retUrl = s.hasNext() ? s.next() : "";
			s.close();
			
		} catch (Exception e) {
			log.error("Problem while getting HTTP from:"+getLinkUrl , e);
		}finally {
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		log.debug("retUrl="+ retUrl);
		return retUrl;
	}


	private String extractDomain(String fullUrl) {
		try {
			URI uri = new URI(fullUrl);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fullUrl;
	}
	
	
	private String makeShortLink(String longLink){
		return linkShortener.makeUrlShort(longLink);
	}


	private String formatDate(String dateStr) {
		String retDate = "לא מוגדר";
				
		try {
			 // *** note that it's "yyyy-MM-dd hh:mm:ss" not "yyyy-mm-dd hh:mm:ss"  
		        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		        Date date = dt.parse(dateStr);

		        // *** same for the format String below
		        SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yyyy");
		        retDate = dt1.format(date);
		} catch (Exception e) {
			log.error("Date cannot be formated. dateStr="+dateStr);
		} 
				
		return retDate;
	}


	public String getImgUrl() {
		return imgUrl;
	}



	public String getStartDate() {
		return startDate;
	}


	public String getEndDate() {
		return endDate;
	}


	public String getTitle() {
		return title;
	}


	public String getDomainName() {
		return domainName;
	}


	public String getMerchantName() {
		return merchantName;
	}


	


	public String getAffUrl() {
		return affUrl;
	}


	@Override
	public String toString() {
		return "OfferPostData [ merchantName=" + merchantName + ",startDate=" + startDate + ", endDate=" + endDate + ", title="
				+ title + ", domainName=" + domainName + ", imgUrl=" + imgUrl
				+ ", affUrl=" + affUrl + "]";
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}


	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}


	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}


	public void setAffUrl(String affUrl) {
		this.affUrl = affUrl;
	}




	public void setCode(String code) {
		this.code = code;
	}


	public String getCode() {
		return code;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	
	
}
