package com.monlinks.dealsposter.dealsposterservice.postsender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a url to a web page, extract a suitable image from that page. This will
 * attempt to follow a method similar to Google+, as described <a href=
 * "http://webmasters.stackexchange.com/questions/25581/how-does-google-plus-select-an-image-from-a-shared-link"
 * >here</a>
 * 
 */
public class ImageExtractor {
	private static final Logger log = LoggerFactory.getLogger(ImageExtractor.class);

	 public static String randomImageUrl(String randomSourceUrl) throws MalformedURLException, IOException{
		 log.debug("randomSourceUrl="+randomSourceUrl);
		 HttpURLConnection connection = (HttpURLConnection) new URL(randomSourceUrl).openConnection();
	     connection.setInstanceFollowRedirects(false);
	     String resRandomUrl = connection.getHeaderField("location");
	     log.debug("resRandomUrl="+resRandomUrl);
		 return resRandomUrl;
	 }
    
	 
	 public static String getHTML(String urlToRead) throws Exception {
	      StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setInstanceFollowRedirects(true);
	
	      System.out.println( "connected url: " + conn.getURL() );
	      
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      return result.toString();
	   }

	 
    public static String extractImageUrl(String url) throws IOException {
        String contentType = new URL(url).openConnection().getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return url;
            }
        }

        //Document document = Jsoup.connect(url).get();
        Document document = Jsoup.connect(url).timeout(3000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
        log.debug(document.toString());
        String imageUrl = null;

        imageUrl = getImageFromOpenGraph(document);
        if (imageUrl != null) {
            return imageUrl;
        }
        
       
              
        imageUrl = getImageFromSchema(document);
        if (imageUrl != null) {
            return imageUrl;
        }

      

        imageUrl = getImageFromTwitterCard(document);
        if (imageUrl != null) {
            return imageUrl;
        }

        imageUrl = getImageFromTwitterShared(document);
        if (imageUrl != null) {
            return imageUrl;
        }

        imageUrl = getImageFromLinkRel(document);
        if (imageUrl != null) {
            return imageUrl;
        }

        imageUrl = getImageFromFirstImage(document);
        if (imageUrl != null) {
            return imageUrl;
        }
        
        imageUrl = getImageFromGuess(document);
        if (imageUrl != null) {
            return imageUrl;
        }

        return imageUrl;
    }

//    private static String getImageFromOpenGraph(Document document) {
//    	Elements metaOgImage = document.select("meta[property=og:image]");
//        if (metaOgImage!=null) {
//        	return metaOgImage.attr("content");
//        }
//		return null;
//	}

    private static String getImageFromFirstImage(Document document) {
    	Element imageElm = document.select("img").first();
    	if (imageElm != null) {
    		return imageElm.absUrl("src");
    	}
    	return null;
    }

	private static String getImageFromTwitterShared(Document document) {
        Element div = document.select("div.media-gallery-image-wrapper").first();
        if (div == null) {
            return null;
        }
        Element img = div.select("img.media-slideshow-image").first();
        if (img != null) {
            return img.absUrl("src");
        }
        return null;
    }

    private static String getImageFromGuess(Document document) {
        // TODO
        return null;
    }

    private static String getImageFromLinkRel(Document document) {
        Element link = document.select("link[rel=image_src]").first();
        if (link != null) {
            return link.attr("abs:href");
        }
        return null;
    }

    private static String getImageFromTwitterCard(Document document) {
        Element meta = document.select("meta[name=twitter:card][content=photo]").first();
        if (meta == null) {
            return null;
        }
        Element image = document.select("meta[name=twitter:image]").first();
        return image.attr("abs:content");
    }

    private static String getImageFromOpenGraph(Document document) {
        Element image = document.select("meta[property=og:image]").first();
        if (image != null) {
            return image.attr("abs:content");
        }
        Element secureImage = document.select("meta[property=og:image:secure]").first();
        if (secureImage != null) {
            return secureImage.attr("abs:content");
        }
        return null;
    }

    private static String getImageFromSchema(Document document) {
        Element container =
            document.select("*[itemscope][itemtype=http://schema.org/ImageObject]").first();
        if (container == null) {
            return null;
        }

        Element image = container.select("img[itemprop=contentUrl]").first();
        if (image == null) {
            return null;
        }
        return image.absUrl("src");
    }
}