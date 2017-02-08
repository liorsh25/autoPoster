package com.monlinks.dealsposter.model;

public class PosterData {
	private String posterId;
	private String posterName;
	private CollectSource collectSource;
	private PublishDest publishDest;
	
	public PosterData(String posterId, String posterName, CollectSource collectSource, PublishDest publishDest) {
		super();
		this.posterId = posterId;
		this.posterName = posterName;
		this.collectSource = collectSource;
		this.publishDest = publishDest;
	}
	public String getPosterId() {
		return posterId;
	}
	public void setPosterId(String posterId) {
		this.posterId = posterId;
	}
	public String getPosterName() {
		return posterName;
	}
	public void setPosterName(String posterName) {
		this.posterName = posterName;
	}
	public CollectSource getCollectSource() {
		return collectSource;
	}
	public void setCollectSource(CollectSource collectSource) {
		this.collectSource = collectSource;
	}
	public PublishDest getPublishDest() {
		return publishDest;
	}
	public void setPublishDest(PublishDest publishDest) {
		this.publishDest = publishDest;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PosterData [posterId=");
		builder.append(posterId);
		builder.append(", posterName=");
		builder.append(posterName);
		builder.append(", collectSource=");
		builder.append(collectSource);
		builder.append(", publishDest=");
		builder.append(publishDest);
		builder.append("]");
		return builder.toString();
	}
	
	
}
