package com;

import java.util.Date;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public final class TweetReader implements StatusListener {

	private final DBHelper db;
	private final ConfigurationBuilder cb;
	private final String consumerKey = "x5R4hzLsACZXQjGsK0u49riNi";
	private final String consumerSecret = "yfl9C3O9mhsm1BinTjD0NfoAy5idZAhVVUbi6xb2RWexiHzgYw";
	private final String accessKey = "2869307315-TAMtDAoMzhiUvgaLjxejTuEyTjvl2XcXaYw4L3X";
	private final String tokenPrivate = "gpvuKSifiNZnwk1egNtVOumZmpNNk6MVXwOMdTL3lPP2X";

	public static void main(String[] args) throws TwitterException {

		TweetReader reader = new TweetReader();
		TwitterStream twitterStream = new TwitterStreamFactory(
				reader.cb.build()).getInstance();
		twitterStream.addListener(reader);
		twitterStream.sample();
	}

	public TweetReader() {
		this.db = new DBHelper();
		this.cb = new ConfigurationBuilder();
		this.cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessKey)
				.setOAuthAccessTokenSecret(tokenPrivate);
	}

	@Override
	public void onStatus(Status status) {
		System.out.println("@" + status.getUser().getScreenName() + " - "
				+ status.getText());
		double latitude = status.getGeoLocation().getLatitude();
		double longitude = status.getGeoLocation().getLongitude();
		long id = status.getId();
		Date timestamp = status.getCreatedAt();
		String user = status.getUser().getScreenName();
		String text = status.getText();
		TweetNode node = new TweetNode(id, user, text, latitude, longitude, timestamp);
		db.insertTweetIntoDB(node);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		System.out.println("Got a status deletion notice id:"
				+ statusDeletionNotice.getStatusId());
		db.deleteTweetWithStatusId(statusDeletionNotice.getStatusId());
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		System.out.println("Got track limitation notice:"
				+ numberOfLimitedStatuses);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		System.out.println("Got scrub_geo event userId:" + userId
				+ " upToStatusId:" + upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		System.out.println("Got stall warning:" + warning);
	}

	@Override
	public void onException(Exception ex) {
		ex.printStackTrace();
	}
}