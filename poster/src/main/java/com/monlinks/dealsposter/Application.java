package com.monlinks.dealsposter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.monlinks.dealsposter.dao.DealsRepository;
import com.monlinks.dealsposter.dealscollectorservice.DealsCollectorJob;
import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.GrouponDealsFetcher;
import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.ICodesDealsFetcher;
import com.monlinks.dealsposter.dealscollectorservice.dealsfeeds.IDealsFetcher;
import com.monlinks.dealsposter.dealsposterservice.DealsPublisherJob;
import com.monlinks.dealsposter.dealsposterservice.linkshortener.GooLinkShortener;
import com.monlinks.dealsposter.dealsposterservice.linkshortener.LinkShortener;
import com.monlinks.dealsposter.model.CategoriesEnums;
import com.monlinks.dealsposter.model.CollectSource;
import com.monlinks.dealsposter.model.CollectType;
import com.monlinks.dealsposter.model.Deal;
import com.monlinks.dealsposter.model.DestinationType;
import com.monlinks.dealsposter.model.Merchant;
import com.monlinks.dealsposter.model.MerchantLocation;
import com.monlinks.dealsposter.model.PosterData;
import com.monlinks.dealsposter.model.PublishDest;
import com.monlinks.dealsposter.model.PublisherType;
import com.monlinks.dealsposter.dealsposterservice.postsender.DealPostData;
import com.monlinks.dealsposter.dealsposterservice.postsender.FacebookPostSender;
import com.monlinks.dealsposter.dealsposterservice.postsender.IPostData;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private DealsRepository repository;

	// private static Logger log =
	// LogManager.getLogger(Application.class.getName());
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Value("${collectDeals.interval.hours}")
	private int collectIntervalHours;

	@Value("${publishDeals.interval.minutes}")
	private long publishIntervalMinutes;
	
	@Value("${fashion.merchants.list}")
	private String fashionMerchantsList;

	@Value("${travel.merchants.list}")
	private String travelMerchantsList;
	


	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		log.debug("spring.config.location="+System.getProperty("spring.config.location"));
		log.debug("server.address="+System.getProperty("server.address"));
				
		System.setProperty("file.encoding", "UTF-8");
//		Properties props = System.getProperties();
//		props.list(System.out);
		
		
				
		printClassPath();
		
		log.debug("**************** START APPLICATION שלום ****************");
		
		
		//collect source: {merchants list / category, value}
		//Publish destination: {facebook,group,groupId} => {[publisherType,detinationType,destinationId]}
		//PosterInfo:
		IDealsFetcher iCodesFetcher = new ICodesDealsFetcher(); 
		IDealsFetcher grouponFetcher = new GrouponDealsFetcher(); 
		
		//FASHION poster
//		CollectSource collectSource = new CollectSource(CollectType.CATEGORY, CategoriesEnums.FASHION.name(), MerchantLocation.ALL,new IDealsFetcher[]{iCodesFetcher});
//		PublishDest publishDest = new PublishDest(PublisherType.FACEBOOK, DestinationType.GROUP, "1821915298095868");
//		PosterData posterData = new PosterData(UUID.randomUUID().toString(), "Fashion for monlinks", collectSource, publishDest);
				
		//TRAVEL category poster
		CollectSource collectSource2 = new CollectSource(CollectType.CATEGORY, CategoriesEnums.TRAVEL.name(), MerchantLocation.ALL,new IDealsFetcher[]{grouponFetcher});
		PublishDest publishDest2 = new PublishDest(PublisherType.FACEBOOK, DestinationType.GROUP, "1807721512802289");
		PosterData posterData2 = new PosterData("TRAVEL_CATEGORY_GROUPON", "Travel for monlinks", collectSource2, publishDest2);
		
		//FASHION list of websites poster
		CollectSource collectSource3 = new CollectSource(CollectType.MERCHANT_LIST, fashionMerchantsList , MerchantLocation.ALL,new IDealsFetcher[]{iCodesFetcher});
		PublishDest publishDest3 = new PublishDest(PublisherType.FACEBOOK, DestinationType.GROUP, "1821915298095868");
		PosterData posterData3 = new PosterData("FASHION_LIST_ICODES", "Fashion list for monlinks", collectSource3, publishDest3);
		
		//TRAVEL list of websites poster
		CollectSource collectSource4 = new CollectSource(CollectType.MERCHANT_LIST, travelMerchantsList , MerchantLocation.ALL,new IDealsFetcher[]{iCodesFetcher});
		PublishDest publishDest4 = new PublishDest(PublisherType.FACEBOOK, DestinationType.GROUP, "1807721512802289");
		PosterData posterData4 = new PosterData("TRAVEL_LIST_ICODES", "Travel list for monlinks", collectSource4, publishDest4);
				
		
		List<PosterData> postersDataList = new ArrayList<PosterData>();
		//postersDataList.add(posterData);
		postersDataList.add(posterData2);//TRAVEL category (groupon)
		postersDataList.add(posterData3);//FASHION list
		postersDataList.add(posterData4);//TRAVEL list

		log.debug("postersDataList="+ postersDataList);
		
		//deleteAllUnpublishedDeals();
		//deleteAllFailedDeals();
		
		scheduelCollectDealsJob(postersDataList);
		scheduelPublishDealsJob(postersDataList);
		
		//testPostLogic();
		//testGetRandomUrl();
		//testGoogleShortener();
		//testMongoSort();

	}

	private void deleteAllFailedDeals() {
		 log.debug("Going to DELETE all failed deals");
		 List<Deal> deletedDeals = repository.deleteByAffUrl("FAILED:null");
		 log.debug("deleted number of deals="+deletedDeals.size());
		 log.debug("deletedDeals="+deletedDeals);		
	}

	private void testMongoSort() {
		List<Deal> unpublishedDeals = null;
		String category = null;
		Sort sortData = new Sort(Direction.ASC, "endDate");//make the early ended deal be first
		
		Pageable pagingData = new PageRequest(0, 5 ,sortData);//will take only the first dealsBulkSize
		unpublishedDeals = repository.findByPostedDateAndCategory(null, "TRAVEL",pagingData);
		int numberOfUnPublishedDeals = unpublishedDeals.size();
		log.debug("Got "+ numberOfUnPublishedDeals +" unpublished Deals for category "+ category );
		
		log.debug("unpublishedDeals="+unpublishedDeals);
		
	
	}

	private void testGoogleShortener() {
		LinkShortener gooLinkShortener = new GooLinkShortener();
		String shortUrl = gooLinkShortener.makeUrlShort("http://prosperent.com/click/api/linkaffiliator/userId/416587/apikey/34e313e356738fa5d0f03cd70db9a8ea/location/http%3A%2F%2Ffacebook.com/url/http%3A%2F%2Fbookit.com");
		log.debug("shortUrl="+shortUrl);
		
	}

	private void scheduelCollectDealsJob(List<PosterData> postersDataList) {
		// shcheduel collect deals
		Timer time = new Timer(); // Instantiate Timer Object
		String collectName = "Apearals";//or marchent name
		DealsCollectorJob dealsCollectorJob = new DealsCollectorJob(postersDataList,repository);

		log.debug("collectIntervalHours=" + collectIntervalHours);
		long collectIntervalMs = collectIntervalHours * 60 * 60 * 1000;
		log.debug("collectIntervalMs=" + collectIntervalMs);

		time.schedule(dealsCollectorJob, 0, collectIntervalMs);

	}

	private void scheduelPublishDealsJob(List<PosterData> postersDataList) {
		// shcheduel post deals
		Timer time2 = new Timer(); // Instantiate Timer Object
		DealsPublisherJob dealsPublisherJob = new DealsPublisherJob(postersDataList,repository);

		log.debug("publishIntervalMinutes=" + publishIntervalMinutes);
		long publishIntervalMs = publishIntervalMinutes * 60 * 1000;
		log.debug("publishIntervalMs=" + publishIntervalMs);

		time2.schedule(dealsPublisherJob, 100 , publishIntervalMs);//wait 100 sec before starting the first time
		
	}

	
	
	 private void printClassPath() {
		 
	        //Get the System Classloader
	        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
	 
	        //Get the URLs
	        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
	 
	        log.debug("ClassPath:");
	        for(int i=0; i< urls.length; i++)
	        {
	            log.debug(urls[i].getFile());
	        }       
	 
	    }
	 
	 
		private void testGetRandomUrl() throws Exception {
			 URL url = new URL("https://source.unsplash.com/random/600x400/?fashion");
		     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		     conn.setInstanceFollowRedirects(true);
		
		     System.out.println( "connected url: " + conn.getURL() );
			
		}

		
	 private void testPostLogic() throws Exception{
		 
		 FacebookPostSender fbPostSender = new FacebookPostSender();
		 
		// boolean checkIsImageTooSmall = fbPostSender.checkIsImageTooSmall(new URL("https://fbexternal-a.akamaihd.net/safe_image.php?d=AQDmHXRkmA-yOM-m&w=130&h=130&url=http%3A%2F%2Fassets.peaceloveworld.com%2Fskin%2Ffrontend%2Fplw%2Fdefault%2Fimages%2Fplw-logo.png&cfs=1&_nc_hash=AQDRjiSLLBlreBto"));
	//{"icid":692577,"merchant_logo_url":"http://merchant.linksynergy.com/fs/logo/lg_1237.jpg","program_id":276223,"expiry_date":"2017-01-09 23:59:59","mid":18393,"description":"Shop Nordstrom to Choose Your Free 15-Piece Gift with Your $125 Skin Care Purchase. Up to $210 Value. Free Shipping. Free Returns","merchant":"Nordstrom","merchant_status":"live","merchant_id":1237,"title":"Skin Care","affiliate_url":"http://shop.nordstrom.com","network":"linkshare","category_id":19,"img_url":"","relationship":"not joined","merchant_url":"http://shop.nordstrom.com","category":"Health_and_Beauty","deep_link":"10047394","start_date":"2017-01-02 00:00:00"},
		 
//		 Merchant merchantAsos = new Merchant("ASOS", "http://www.asos.com", MerchantLocation.US, null);
//		Deal deal1 = new Deal("2578774", merchantAsos, "Up To 70% Off Sale - (ASOS Final End Of Year SALE)", null, null, null, null, "2017-01-04 00:00:00", "2017-01-09 08:00:00", "Up To 70% Off Sale", null, null, null, null);
//		log.debug(deal1.toString());
		
		
		 Merchant merchantNord = new Merchant("Walmart", "http://walmart.com", MerchantLocation.US, null);
		//Deal deal2 = new Deal("1111111", merchantNord, "Shop Nordstrom to Choose Your Free 15-Piece Gift with Your $125 Skin Care Purchase. Up to $210 Value. Free Shipping. Free Returns", null, null, null, null, "2017-01-02 00:00:00", "2017-01-09 23:59:59", "Skin Care", null, null, null, null);
		//log.debug(deal2.toString());
		//2017-01-05 15:16:51,661 DEBUG [Timer-0]- c.m.d.d.DealsCollectorJob::handleCollect(DealsCollectorJob.java:77)  SAVED the deal:Deal [extId=2578774, merchant=Merchant [name=ASOS, domain=http://www.asos.com, location=US, imageUrl=null], description=Up To 70% Off Sale - (ASOS Final End Of Year SALE), code=null, discount=null, title=Up To 70% Off Sale, dealImageUrl=, postedDate=null, startDate=2017-01-04 00:00:00, endDate=2017-01-09 08:00:00, affUrl=null, collectDate=2017-01-05T15:13:56.804, dealSource=icodes, category=null] 
		// IPostData dealToPost = new DealPostData(deal2);
		//fbPostSender.postTheDeal(dealToPost,DestinationType.GROUP,"1821915298095868");
		 
		 //DealPostData postDataObj = new DealPostData(null);
//		 postDataObj.setAffUrl("http://prosperent.com/api/linkaffiliator/redirect?apiKey=34e313e356738fa5d0f03cd70db9a8ea&location=http%3A%2F%2Ffashion.com&url=http%3A%2F%2Fwww.martofchina.com");
//		 postDataObj.setCode("CODE_TEST3");
//		 postDataObj.setMerchantName("martofchina.com");
//		 postDataObj.setDomainName("martofchina.com");
		 
		 //String affLink = postDataObj.buildAffUrl("http://childrensplace.com");
		// log.debug("affLink="+affLink);
		
		 
	 }
	 
	 
	 public void deleteAllUnpublishedDeals(){
		 log.debug("Going to DELETE all unpublished deals");
		 List<Deal> deletedDeals = repository.deleteByPostedDate(null);
		 log.debug("deleted number of deals="+deletedDeals.size());
		 log.debug("deletedDeals="+deletedDeals);
	 }

}
