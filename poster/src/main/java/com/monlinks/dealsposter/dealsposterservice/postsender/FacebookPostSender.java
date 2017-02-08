package com.monlinks.dealsposter.dealsposterservice.postsender;

import java.awt.Image;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.monlinks.dealsposter.model.DestinationType;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Post;
import facebook4j.PostUpdate;
import facebook4j.PrivacyBuilder;
import facebook4j.PrivacyParameter;
import facebook4j.PrivacyType;
import facebook4j.RawAPIResponse;
import facebook4j.Reading;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

@Configurable
public class FacebookPostSender extends PostSender {
	
	
	
	Facebook facebook = null;
	String appId = "1286867134668803";
	String appSecret = "a91b071cf2f2ddc17e8d88f47c1d6682";
	String commaSeparetedPermissions = "user_managed_groups, user_about_me, publish_actions, public_profile";
	//TODO: take from configuration
	String accessToken = "EAASSZAhpvYAMBALVjJDX2Loh90fSDBWZA7mQKts6P2dgsoPO3aJ3BEupUZCyNtQqYIXtRhhQEtZBX34EAZAd8A1Lhz6jdZAa2kHivuK60SmI0ydhdJQ1ecLXLf5GZAAcTccn4JMsNRgvpptIoX8Su099qBZCrI2PqfMZD";//appId+"|"+appSecret;//lior:"EAASSZAhpvYAMBABDJUf1dlomiN10brErAec74n3QeZAsNTuWtTPIXIuwk1cSYacFwYKQk7RE21RwrvX7RIHsC37JtK4SO1QYxO2qUZCyqhQuMICYr5MSjjePTZBZAFYC49bex9ZAhH6Cgr8DfcHGbM";//appId+"|"+appSecret;//"EAASSZAhpvYAMBABrTUqfZABIAEY9gLxJYORUEJ19mmvIvt7be8h2pvFZBfSldos8VNkjPsqYpkCtWpQasZAr6g5zR91Ic5YYLvMBiq0oJBIgln7SHFXoJJKNN1isiwWFY7jQgAxmvNNkjSsB3Avm";
	String DammyfacebookGroupId = "1821915298095868";//fashion groupid - just for temp post
	
	private static final Logger log = LoggerFactory.getLogger(FacebookPostSender.class);
	
	
	public FacebookPostSender() {
		super();
		facebook = new FacebookFactory().getInstance();
		//reloadAccessToken();
		loadAccessToken();
		
	}
	
	//@PostConstruct
	public void loadAccessToken(){
		facebook.setOAuthAppId(appId, appSecret);
		facebook.setOAuthPermissions(commaSeparetedPermissions);
		log.debug("SET accessToken="+accessToken);
		facebook.setOAuthAccessToken(new AccessToken(accessToken, null));
	}
	
		
	private void reloadAccessToken(){
		try{
			log.debug("Try to reload Access Token with: appId="+ appId +",appSecret="+appSecret);
			facebook.setOAuthAppId(appId, appSecret);
			facebook.setOAuthPermissions(commaSeparetedPermissions);
			AccessToken accessToken = facebook.getOAuthAppAccessToken();
			log.debug("GOT accessToken="+accessToken);
			facebook.setOAuthAccessToken(accessToken);
		} catch (FacebookException e) {
			log.error("Problem in getting new access token.",e);
		}

	}
	
	public void postTheDeal(IPostData postDataObj,DestinationType detinationType,String destinationId) throws Exception{
//		if(accessToken==null){
//			loadAccessToken();
//		}
		
		postAsFeed(postDataObj,detinationType,destinationId);

		//postAsLink(postDataObj);	
	}
	
	private void postAsFeed(IPostData postDataObj, DestinationType detinationType, String destinationId) throws Exception {
		
		PostUpdate couponPost = createCouponPost((DealPostData) postDataObj);				
		
		String postId = postToFacebook(couponPost, detinationType,  destinationId);
		
		Post postDataFromFB = facebook.getPost(postId,new Reading().fields("picture").addParameter("type", "large"));
		log.debug("Returned post data:"+ postDataFromFB);
		
		URL finalPostUrl = postDataFromFB.getPicture();
		log.debug("Post image URL="+ postDataFromFB.getPicture());
		
		//if for some reason the final post image is too small, delete the post and replace it with random image from category
		if(checkIsImageTooSmall(finalPostUrl)){
			log.debug("Final post image is too small, replace with random image");
			String replacedPostId = replacePostWithRandomImage(postId,couponPost,((DealPostData) postDataObj).getCategory(),detinationType,destinationId);
			log.debug("replacedPostId="+ replacedPostId);
		}
	
		
	}
	
	private String postToFacebook(PostUpdate couponPost, DestinationType detinationType, String destinationId) throws FacebookException {
		String postId = null;
		log.debug("Going to POST:"+ printPostData(couponPost) );
		
		if(detinationType.equals(DestinationType.GROUP)){
			postId = facebook.postGroupFeed(destinationId, couponPost);//destinationId => groupId
			log.debug("PostGroupFeed to facebook was successfull. postID="+postId);
			
		}else{//TODO: handle post to page
			//postId = post to page
			//log.debug("PostToPage to facebook was successfull. postID="+postId);
		}
		
		return postId;
	}

	private String replacePostWithRandomImage(String postIdToDelete, PostUpdate couponPost, String category, DestinationType detinationType, String destinationId) {
		String postId = null;
		try {
			//delete the post
			facebook.deletePost(postIdToDelete);
			log.debug("Post was deleted. postIdToDelete ="+postIdToDelete);
			
			URL randomImage = getRandomImageByCategory(category);
			
			couponPost.setPicture(randomImage);
			
			postId = postToFacebook(couponPost, detinationType, destinationId);
			
		} catch (FacebookException|IOException e) {
			log.error("Problem to replacePostWithRandomImage",e);
		} 
		
		return postId;
		
	}

	private URL getCorrectLinkImage(DealPostData postDataObj) throws Exception {
		URL retImageUrl = null;
		String domainName = postDataObj.getDomainName();
		
		if(postDataObj.getImgUrl()!= null && !postDataObj.getImgUrl().isEmpty()){
			retImageUrl =  new URL(postDataObj.getImgUrl()); // might be already an image from the Deal itself
			log.debug("Image from the original deal:"+ retImageUrl);
		}
		
		if(checkIsImageTooSmall(retImageUrl)){
			retImageUrl = getImageFromFakePost(domainName);
			log.debug("Image from fake post:"+ retImageUrl);
			
			if(checkIsImageTooSmall(retImageUrl))	{
				retImageUrl = getAlternativeImage(postDataObj);//logo or random image
			}
			
		}	
				
		log.debug("FINAL image to be used in post:"+ retImageUrl);	
		
		return retImageUrl;
	}
	
	private URL getAlternativeImage(DealPostData postDataObj){
		URL retImageUrl = null;
		try {
			//Try to take a logo
			String logoUrl = "http://logo.clearbit.com/"+postDataObj.getDomainName()+"?size=400";
			log.debug("Try logoUrl="+ logoUrl);
			retImageUrl = new URL(logoUrl);

			if(checkIsImageTooSmall(retImageUrl)){
				retImageUrl = getRandomImageByCategory(postDataObj.getCategory());
			}

		} catch (IOException e) {
			log.error("Problem in getting image from URL:"+ retImageUrl,e);
		}

		log.debug("return: retImageUrl="+ retImageUrl);
		return retImageUrl;
	}

	
	
	private URL getRandomImageByCategory(String category) throws MalformedURLException, IOException {
		String categoryToUse = category==null?"fashion":category;//TODO: make the category to be send for merchant list also
		String randomImageSourceUrl = "https://source.unsplash.com/random/600x400/?"+ categoryToUse ;
		log.debug("Try to get random image from:"+ randomImageSourceUrl );
		String randImageUrlStr = ImageExtractor.randomImageUrl(randomImageSourceUrl);//get random image
		URL randImageUrl = new URL(randImageUrlStr);
		log.debug("randImageUrl="+ randImageUrl);	
		return randImageUrl;
	}

	private URL getImageFromFakePost(String domainName) throws MalformedURLException, FacebookException {
		URL retImageUrl = null;
		log.debug("Going to post a FAKE post for "+ domainName);
		//try {	
			//First post a link in private 
			String url = "http://"+domainName;
			String linkPostId;

			try{
				linkPostId = facebook.postGroupLink(DammyfacebookGroupId, new URL(url ));
			}catch(FacebookException|IllegalStateException e){
				log.error("Failed in postGroupLink",e);
//				if((e instanceof IllegalStateException) || e.getMessage().contains("code - 190")){
//					reloadAccessToken();
//					log.debug("Try to send again postGroupLink");
//					linkPostId = facebook.postGroupLink(DammyfacebookGroupId, new URL(url ));
//					//if there will be a problem here will throw the exception up
//				}else{
//					throw e;
//				}
				throw e;
			}

			log.debug("Finish post a link to "+url+".linkPostId="+linkPostId);

			//get the image
			Post postDataFromFB = facebook.getPost(linkPostId,new Reading().fields("picture").addParameter("type", "large"));
			log.debug("Returned post data:"+ postDataFromFB);
			retImageUrl = postDataFromFB.getPicture();
			log.debug("retImageUrl="+ retImageUrl);

			//delete the post
			facebook.deletePost(linkPostId);
			log.debug("Post was deleted. linkPostId ="+linkPostId);

//		} catch (FacebookException e) {
//			log.warn("Fail to post a FAKE post with link: http://"+domainName,e );
//		}
		return retImageUrl;
	}

//	private String checkImageAndUpdatePost(PostUpdate couponPost, String postId, String imgUrl) throws MalformedURLException, FacebookException{
//		if(imgUrl==null || imgUrl.isEmpty()) return postId;
//		
//		URL picture = new URL(imgUrl);
//		String retPostId = postId;
//		if(checkIsImageTooSmall(picture)){
//			log.debug("Logo image is too small, will not replace the post. imge logo url="+ imgUrl);
//		}else{
//			log.debug("Logo image size is good enough, going to replace the post. imge logo url="+ imgUrl);
//			
//			retPostId = updatePostWithNewImage(postId,couponPost,picture);
//			
//		}
//		
//		return retPostId;
//	}
	
	
	

//	private String updatePostWithNewImage(String postId, PostUpdate couponPost, URL newPicture) throws FacebookException {
//		//update the post in facebook
//		//delete current post
//		facebook.deletePost(postId);
//		log.debug("Post was deleted. postID="+postId);
//		
//		log.debug("update post data with new picture:" + newPicture);
//		couponPost.setPicture(newPicture);
//		
//		log.debug("Going to rebuplish the post with the updated picture");
//		String postId2 = facebook.postGroupFeed(facebookGroupId, couponPost);
//		log.debug("PostGroupFeed to facebook was successfull. postID="+postId2);
//		
//		
//		boolean isNewPostShowImage = isPostShowImage(postId2);
//		log.debug("isNewPostShowImage="+isNewPostShowImage);
//		
//		return postId2;
//		
//	}

//	private boolean isPostShowImage(String postId) throws FacebookException {
//		boolean retAns = false;
//		Post postDataFromFB = facebook.getPost(postId,new Reading().fields("picture"));
//		log.debug("Returned post data:"+ postDataFromFB);
//		URL pictureInPost = postDataFromFB.getPicture();
//		if(pictureInPost!=null){
//			//BAD: https://fbexternal-a.akamaihd.net/safe_image.php?d=AQDmHXRkmA-yOM-m&w=130&h=130&url=http%3A%2F%2Fassets.peaceloveworld.com%2Fskin%2Ffrontend%2Fplw%2Fdefault%2Fimages%2Fplw-logo.png&cfs=1&_nc_hash=AQDRjiSLLBlreBto
//			//GOOD: https://fbexternal-a.akamaihd.net/safe_image.php?d=AQByLx7OoHq8gtVv&w=476&h=249&url=http%3A%2F%2Fmediaus.topman.com%2Fwcsstore%2FConsumerDirectStorefrontAssetStore%2Fimages%2Fcolors%2Fcolor9%2Fcms%2Fpages%2Fstatic%2Fstatic-0000097180%2Fimages%2FWK16-best-of-street-style-UK-US-AUS.jpg&cfs=1&upscale=1&sx=17&sy=0&sw=956&sh=500&_nc_hash=AQD4_fIONaJ-XanH
//			boolean isImageTooSmall = checkIsImageTooSmall(pictureInPost);
//			log.debug("isImageTooSmall="+ isImageTooSmall);
//			if(!isImageTooSmall){
//				retAns = true;
//			}
//		}
//		
//		log.debug("isPostShowImage ret="+ retAns);
//		return retAns;
//	}

	private boolean checkIsImageTooSmall(URL imageUrlToTest) {
		
		boolean retAns = false;
		
		if(imageUrlToTest == null || "".equals(imageUrlToTest)){
			log.debug("imageUrlToTest is null. return checkIsImageTooSmall=true");
			return true;
		}
	
		log.debug("Going to check dimentions of image from post:"+ imageUrlToTest);
		Image image = new ImageIcon(imageUrlToTest).getImage();
		if(image==null){
			log.debug("The url "+ imageUrlToTest +" return no image. return checkIsImageTooSmall=true");
			return true;//no image means its too small
		}
		
		int imgWidth = image.getWidth(null);
		int imgHeight = image.getHeight(null);
		log.debug("imgWidth="+ imgWidth +" , imgHeight="+imgHeight);
		
		log.debug("Less then 130 will be considered as small");
		retAns = (imgWidth<130 || imgHeight<130);
		if(log.isDebugEnabled()){
			if(retAns){
				log.debug("The image "+imageUrlToTest +", is too SMALL.");
			}else{
				log.debug("The image "+imageUrlToTest +", is in GOOD size.");
			}
		}
		
	
		return retAns;
	}

	private String printPostData(PostUpdate couponPost) {
		 return "PostUpdate{" +
	                "message='" + couponPost.getMessage() + '\'' +
	                ", link=" + couponPost.getLink() +
	                ", picture=" + couponPost.getPicture() +
	                ", name='" + couponPost.getName() + '\'' +
	                ", caption='" + couponPost.getCaption() + '\'' +
	                ", description='" + couponPost.getDescription() + '\'' +
	                ", actions=" + couponPost.getActions() +
	                ", place='" + couponPost.getPlace() + '\'' +
	                ", tags='" + couponPost.getTags() + '\'' +
	                ", privacy=" + couponPost.getPrivacy() +
	                ", objectAttachment='" + couponPost.getObjectAttachment() + '\'' +
	                ", targeting=" + couponPost.getTargeting() +
	                ", published=" + couponPost.getPublished() +
	                ", scheduledPublishTime=" + couponPost.getScheduledPublishTime() +
	                '}';
	}

	private void postAsLink(IPostData postDataObj) throws MalformedURLException, FacebookException {
		String affUrl = postDataObj.getAffUrl();
		log.debug("affUrl="+affUrl);
		String msgTosend =  buildLinkMessage(postDataObj);
		log.debug("msgTosend="+msgTosend);
		//facebook.postGroupLink(facebookGroupId, new URL(affUrl), msgTosend);
	}


	private String buildLinkMessage(IPostData postDataObj){
		String firstLine = "קוד קופון לאתר "+ makeItNotLinkable(postDataObj.getMerchantName())+" , העתיקו את קוד הקופון "+postDataObj.getCode()+" והכנסו מכאן לאתר.";
		String secondLine =  postDataObj.getTitle();
		String thirdLine = "קוד קופון: "+postDataObj.getCode()+" |  תוקף: "+postDataObj.getStartDate()+"-"+postDataObj.getEndDate();
		String msgToPost = firstLine+"\r\n "+secondLine+"\r\n "+thirdLine;
		log.debug("msgToPost = "+ msgToPost);
		return msgToPost;
	}
	
	private PostUpdate createCouponPost(DealPostData postDataObj) throws Exception {

		String noDate = "לא מוגדר";
		String startDate = postDataObj.getStartDate() == null?noDate:postDataObj.getStartDate();
		String endDate = postDataObj.getEndDate()== null?noDate:postDataObj.getEndDate();
		String codeDescTemplate = "";
		if(postDataObj.getCode() != null){
			codeDescTemplate = "קוד קופון: "+ postDataObj.getCode()+" | ";
		}
		String validTimeTemplate = "תוקף: "+startDate+"-"+endDate;
		String buttomDesc = codeDescTemplate + validTimeTemplate;
				
		PostUpdate post = new PostUpdate(new URL(postDataObj.getAffUrl()))
         .name(postDataObj.getTitle())       
         .caption(postDataObj.getDomainName())
         .description(buttomDesc);
		if(postDataObj.getCode() == null){
			//נהדר , שווה, משתלם , לזמן מוגבל , 
			post.message("מבצע באתר "+ makeItNotLinkable(postDataObj.getMerchantName())+", לפרטים נוספים הכנסו מכאן לאתר.\nתאור המבצע:\n"+ makeItNotLinkable(postDataObj.getDescription()));
		}else{
			post.message("קוד קופון לאתר "+ makeItNotLinkable(postDataObj.getMerchantName())+", העתיקו את קוד הקופון "+postDataObj.getCode()+" והכנסו מכאן לאתר.\nתאור הקופון:\n"+ makeItNotLinkable(postDataObj.getDescription()));
		}
		
		//build the correct image url
		URL picture = getCorrectLinkImage(postDataObj);
		if(picture!=null){
			post.setPicture(picture);
			postDataObj.setImgUrl(picture.toString());//to be saved in database
		}else{
			log.error("getCorrectLinkImage returned null for postDataObj="+postDataObj);
		}
				
		return post;
	}



	private String scrapImageFromUrl(String webPageUrl) {
		String retImageUrl = null;
		log.debug("Try to scrap image for url:"+ webPageUrl);
		try {
			RawAPIResponse callGetAPI = facebook.callPostAPI("?scrape=true&id="+webPageUrl);//("",new HttpParameter("scrap", true),new HttpParameter("id", "http://gearxs.com/"));
			JSONObject asJSONObject = callGetAPI.asJSONObject();
			log.debug("Response from Facebook:"+ asJSONObject);
			JSONArray imgArr = (JSONArray) asJSONObject.get("image");
			if(imgArr != null && imgArr.length()>0){
				JSONObject imgObj = (JSONObject) imgArr.get(0);
				retImageUrl = (String) imgObj.get("url");
			}
			
		} catch (FacebookException|JSONException e) {
			log.error("problem get image from url:"+webPageUrl ,e);
		} 
		
		return retImageUrl;
	}

	private String makeItNotLinkable(String merchantName) {
		return merchantName.replaceAll(".com", " .com");
	}

	
	

}
