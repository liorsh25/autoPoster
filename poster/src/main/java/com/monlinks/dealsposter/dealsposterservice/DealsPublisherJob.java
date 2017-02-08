package com.monlinks.dealsposter.dealsposterservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.monlinks.dealsposter.dao.DealsRepository;
import com.monlinks.dealsposter.model.CollectType;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.PosterData;
import com.monlinks.dealsposter.model.PublishDest;
import com.monlinks.dealsposter.dealsposterservice.postsender.DealPostData;
import com.monlinks.dealsposter.dealsposterservice.postsender.FacebookPostSender;
import com.monlinks.dealsposter.dealsposterservice.postsender.IPostData;
import com.monlinks.dealsposter.dealsposterservice.postsender.IPostSender;

import facebook4j.FacebookException;

public class DealsPublisherJob extends TimerTask {
	private DealsRepository repository;
	
	private IPostSender facebookSender = new FacebookPostSender();
	
	private static Logger log = LogManager.getLogger(DealsPublisherJob.class.getName());
	

	private   int dealsBulkSize;
	private  int delayBetweenPostsSec;
	private List<PosterData> postersDataList;

//	@Autowired
//	public DealsPublisherJob(@Value("${publishDeals.bulk.size}") int dealsBulkSize,@Value("${publishDeals.delay.between.posts.sec}") int delayBetweenPostsSec) {
//		super();
//		this.dealsBulkSize = dealsBulkSize;
//		this.delayBetweenPostsSec = delayBetweenPostsSec;
//	}


	public DealsPublisherJob(List<PosterData> postersDataList, DealsRepository repository) {
		this.repository = repository;
		//updatePropValues();
		ResourceBundle config = ResourceBundle.getBundle("application",Locale.ROOT);
		dealsBulkSize =  Integer.parseInt(config.getString("publishDeals.bulk.size"));
		delayBetweenPostsSec = Integer.parseInt(config.getString("publishDeals.delay.between.posts.sec"));
		this.postersDataList = postersDataList;
	}

	public void updatePropValues() {
		 
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";
			log.debug("propFileName="+propFileName);
			
			inputStream = getClass().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			dealsBulkSize =  Integer.parseInt(prop.getProperty("publishDeals.bulk.size"));
			delayBetweenPostsSec = Integer.parseInt(prop.getProperty("publishDeals.delay.between.posts.sec"));
 
		
		} catch (Exception e) {
			log.error("Problem in getting properties from application.properties",e );
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				log.error("Error close inputStream",e);
			}
		}

	}

	
	public void run() {
		log.debug("============== START DealsPublisherJob ================");
		for (PosterData posterData : postersDataList) {
			handlePublish(posterData);
		}

		
	}


	private void handlePublish(PosterData posterData) {
		
		try {
			//List<Deal> unpublishedDeals = repository.findByPostedDate(null);//get all unposted deals
			log.debug("START: Handle PUBLISH for poster:"+ posterData);
			
			List<Deal> unpublishedDeals = null;
			//String category = null;
			Sort sortData = new Sort(Direction.ASC, "endDate");//make the early ended deal be first
			Pageable pagingData = new PageRequest(0, dealsBulkSize,sortData);//will take only the first dealsBulkSize
			
			unpublishedDeals = repository.findByPostedDateAndPosterId(null, posterData.getPosterId() ,pagingData);
			
//			if(posterData.getCollectSource().getCollectType().equals(CollectType.CATEGORY)){
//				category = posterData.getCollectSource().getCollectValue();
//			
//				unpublishedDeals = repository.findByPostedDateAndCategory(null, category,pagingData);
//			}else{
//				unpublishedDeals = repository.findByPostedDateAndCategory(null, null,pagingData);
//			}
			
			int numberOfUnPublishedDeals = unpublishedDeals.size();
			log.debug("Got "+ numberOfUnPublishedDeals +" unpublished Deals for PosterID "+ posterData.getPosterId() );
			
			if(numberOfUnPublishedDeals==0){
				log.warn("No unpublished Deals were found. exit the job.");
				return;
			}else{
				log.debug("unpublishedDeals="+unpublishedDeals);
			}
			
			List<Deal> publishedDeals = publishAllDeals(unpublishedDeals,posterData.getPublishDest());
			
			log.debug("FINISH publishing all bulk unpublished deals");
		
			log.debug("Going to save these " + publishedDeals.size() + " published deals:" + publishedDeals);
			repository.save(publishedDeals);
			log.debug("FINISH: Handle PUBLISH for poster:"+ posterData);

		} catch (Exception e) {
			log.error("Problem in DealsPublisherJob" , e);
		}
		
	}

	private List<Deal> publishAllDeals(List<Deal> unpublishedDeals, PublishDest publishDest) throws FacebookException, IOException, InterruptedException {
		
		log.debug("Going to publish "+ dealsBulkSize +" unpublished Deals");
		List<Deal> publishedDeals = new ArrayList<Deal>();

		for (int i = 0; i < dealsBulkSize && i<unpublishedDeals.size(); i++) {
			Deal deal = unpublishedDeals.get(i);
			log.debug("********** START: publish deal "+ deal.getExtId() +" for website "+ deal.getMerchant().getHomePageUrl() +"****************");
			try{
				//TODO: if deal endDate passed current date don't publish it.
				IPostData dealToPost = new DealPostData(deal);
				facebookSender.postTheDeal(dealToPost,publishDest.getDetinationType(),publishDest.getDestinationId());
				//only if the publish was successful update the post time and save in DB
				deal.setPostedDate(LocalDateTime.now().toString());
				deal.setAffUrl(dealToPost.getAffUrl());
				
				publishedDeals.add(deal);
				log.debug("Deal was published: " + deal);
			}catch(Exception e){				
				log.error("Problem publish deal "+deal ,e);
				//update the failed information
				deal.setPostedDate(LocalDateTime.now().toString());
				if(e instanceof FacebookException){
					deal.setAffUrl("FAILED: code"+ ((FacebookException)e).getErrorCode());
				}else{
					deal.setAffUrl("FAILED:"+ e.getMessage() );
				}
				
				publishedDeals.add(deal);
			}
			
			log.debug("********** END: publish deal "+ deal.getExtId() +" ****************");
			
			log.debug("Delay before next post: delayBetweenPostsSec= " + delayBetweenPostsSec);
			Thread.sleep(delayBetweenPostsSec * 1000);
		}
		
		return publishedDeals;
	}
	
	

	public int getDealsBulkSize() {
		return dealsBulkSize;
	}

	public void setDealsBulkSize(int dealsBulkSize) {
		this.dealsBulkSize = dealsBulkSize;
	}

	public int getDelayBetweenPostsSec() {
		return delayBetweenPostsSec;
	}

	public void setDelayBetweenPostsSec(int delayBetweenPostsSec) {
		this.delayBetweenPostsSec = delayBetweenPostsSec;
	}


}
