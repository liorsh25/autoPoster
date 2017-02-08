package com.monlinks.dealsposter.dealsposterservice.postsender;

import com.monlinks.dealsposter.model.DestinationType;

public interface IPostSender {

	void postTheDeal(IPostData postDataObj,DestinationType detinationType,String destinationId) throws Exception;
}
