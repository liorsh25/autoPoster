package com.monlinks.dealsposter.dealsposterservice.linkshortener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class BitlyLinkShortener implements LinkShortener {

	String bitlyAccessToken = "7506802060aaeb00e99ff903e8e1ea6f144717c7";
	BitlyClient client = null;
	private static final Logger log = LoggerFactory.getLogger(BitlyLinkShortener.class);
	
	
	public BitlyLinkShortener() {
		client = new BitlyClient(bitlyAccessToken);
	}



	public String makeUrlShort(String longUrl){
		
		String shortUrl = longUrl;//use the long URL in case we fail
		log.debug("longUrl="+longUrl);
		Response<ShortenResponse> respShort = client.shorten().setLongUrl(longUrl).call();
		log.debug("respShort="+respShort);
		if(respShort.status_code == 200){
			shortUrl = respShort.data.url;
		}
		
		log.debug("shortUrl="+shortUrl);
		return shortUrl;
		
	}

}
