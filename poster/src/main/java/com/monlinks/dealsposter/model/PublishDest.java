package com.monlinks.dealsposter.model;

public class PublishDest {
	//{facebook,group,groupId} => {[publisherType,detinationType,destinationId]}
	private PublisherType publisherType;
	private DestinationType detinationType;
	private String destinationId;
	
	public PublishDest(PublisherType publisherType, DestinationType detinationType, String destinationId) {
		super();
		this.publisherType = publisherType;
		this.detinationType = detinationType;
		this.destinationId = destinationId;
	}

	public PublisherType getPublisherType() {
		return publisherType;
	}

	public void setPublisherType(PublisherType publisherType) {
		this.publisherType = publisherType;
	}

	public DestinationType getDetinationType() {
		return detinationType;
	}

	public void setDetinationType(DestinationType detinationType) {
		this.detinationType = detinationType;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PublishDest [publisherType=");
		builder.append(publisherType);
		builder.append(", detinationType=");
		builder.append(detinationType);
		builder.append(", destinationId=");
		builder.append(destinationId);
		builder.append("]");
		return builder.toString();
	}

	
	
	
	
}
