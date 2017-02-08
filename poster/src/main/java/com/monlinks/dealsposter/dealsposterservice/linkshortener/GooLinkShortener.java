package com.monlinks.dealsposter.dealsposterservice.linkshortener;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.model.Url;


public class GooLinkShortener implements LinkShortener {

	String gooAccessToken = "AIzaSyCVt0Ax2_1cFySQZegY1wWOfTPNobfMjzQ";//from: https://console.developers.google.com/apis/credentials?project=monlinks-155314&authuser=1
	Urlshortener shortener = null;
	private static final Logger log = LoggerFactory.getLogger(GooLinkShortener.class);
	
	public GooLinkShortener() {
		 shortener =  buildUrlShortener();
	}

	

	private Urlshortener buildUrlShortener()  {
		Urlshortener urlshortener = null;
		GoogleCredential credential = new GoogleCredential();
	   	HttpTransport transporter;
		try {
			transporter = GoogleNetHttpTransport.newTrustedTransport();
		
		JsonFactory jsonFactory=JacksonFactory.getDefaultInstance();
		// Urlshortener 
	    Urlshortener.Builder builder = new Urlshortener.Builder(transporter, jsonFactory, credential);
	   
	    urlshortener = builder.build();
		} catch (GeneralSecurityException | IOException e) {
			log.error("Problem in url shortener",e);
		}
		
		return urlshortener;
	}



	@Override
	public String makeUrlShort(String longUrl){
		
		String shortUrl = longUrl;//use the long URL in case we fail
		log.debug("longUrl="+longUrl);
		
		Url toInsert = new Url().setLongUrl(longUrl);
		 Url recivedUrl = null;
	    try {
	       recivedUrl = shortener.url().insert(toInsert).setKey(gooAccessToken).execute();
	    } catch (IOException e) {
	      log.error("Failed to shorten this long url: "+ longUrl,e);
	    } 
		
		log.debug("recivedUrl="+recivedUrl);
		if(recivedUrl != null){
			shortUrl = recivedUrl.getId();
		}else{
			log.error("recivedUrl was not recived correctly. recivedUrl="+recivedUrl);
		}
		
		log.debug("shortUrl="+shortUrl);
		return shortUrl;
		
	}

}
