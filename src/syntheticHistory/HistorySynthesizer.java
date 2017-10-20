package syntheticHistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.List;


import org.json.simple.JSONArray;  
import org.json.simple.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class HistorySynthesizer {
	
	//The number of times userTimeline has been called
	int userTimelineCallCount = 0;
	
	//The number of times getFriends has been called
	int getFriendsCallCount = 0;

	//A list of tweets for a user 
	Hashtable<String, ArrayList<Status>> userTweets = new Hashtable<String, ArrayList<Status>>();
	
	//A hashtable to store the friends of a friend
	Hashtable<String, ArrayList<User>> friendsOfFriend = new Hashtable<String, ArrayList<User>>();
	
	//A set of twitter objects
	private Set<Twitter> twitterConnections;
	
	ArrayList<String> users = new ArrayList<String>();
	
	public static void main(String[] args) {

		HistorySynthesizer x = new HistorySynthesizer();
		x.getRandomUsers();
		
//		String users[] = {"FireflyAMV", "thesoulofwind1", "alexhammersmith", "Whataburger", "AlexMSmithAutho", "JoelHeyman", "VernonDavis85", "KirkCousins8", "FredoSauce", "Prudential", 
//						"TheGiftedonFOX", "AishaMotan", "EDMShiro", "john_keim", "thecooleyzone", "Walterejones", "Areeba_Javed", "stephenpaddock", "WWERollins", "WWEDanielBryan"};
//		
//		
//		for (String user : users) {
//			//x.writeOutFriendsListForUser(user);
//			x.createSyntheticHistoryForUser(user);
//		}
		
		
		
	}
	
	
	/**
	 * Default initializer. Creates twitter connections based on information in a txt document.
	 */
	public HistorySynthesizer() {
		twitterConnections = new HashSet<Twitter>();
		twitterConnections.add(TwitterFactory.getSingleton());
		
		
		initTwitterConnections("config2.txt");
	}
	
	public void getRandomUsers() {
		TwitterStream twitterStream = TwitterStreamFactory.getSingleton();

		twitterStream.addListener(new StatusListener () {
			public void onStatus(Status status) {
				System.out.println(status.getUser().getScreenName()); // print tweet text to console
				System.out.println(status.getText()); // print tweet text to console
				users.add(status.getUser().getScreenName());
				
				if (users.size() == 1100) 
					twitterStream.shutdown();
			}

			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub
				waitSec(10);
			}
		});
		
		FilterQuery tweetFilterQuery = new FilterQuery(); // See 

		tweetFilterQuery.language(new String[]{"en"}); // Note that language does not work properly on Norwegian tweets 

 
		twitterStream.sample();
		int currentUserIndex = 0;
				
		while(currentUserIndex < 1000) {
			waitSec(10);
			
			if (currentUserIndex < users.size()) {
				this.createSyntheticHistoryForUser(users.get(currentUserIndex));
				currentUserIndex++;
			}
		}
		
		

	}



	private void initTwitterConnections(String fileName) {
		BufferedReader br = null;
		try {
			String line = null;
			String[] lines = new String[4];
			int linesIndex = 0;
			br = new BufferedReader(new FileReader(fileName));
			
			while ((line = br.readLine()) != null) {
				if (linesIndex == 4) {
					createAndAddTiwtterConnections(lines);
					linesIndex = 0;
				}
				lines[linesIndex] = line;
				linesIndex++;
			}
			
			if (linesIndex == 4) {
				createAndAddTiwtterConnections(lines);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void createAndAddTiwtterConnections(String[] lines) {
		ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
		twitterConfigBuilder.setDebugEnabled(true);
		
	    for (int i = 0; i < lines.length; ++i) {
	        String[] input = lines[i].split("=");

	        if (input[0].equalsIgnoreCase("consumerkey")) {
	            twitterConfigBuilder.setOAuthConsumerKey(input[1]);
	            System.out.println("Consumer Key: " + input[1]);
	        }
	        if (input[0].equalsIgnoreCase("consumersecret")) {
	            twitterConfigBuilder.setOAuthConsumerSecret(input[1]);
	            System.out.println("Consumer Secret: " + input[1]);
	        }
	        if (input[0].equalsIgnoreCase("accesstoken")) {
	            twitterConfigBuilder.setOAuthAccessToken(input[1]);
	            System.out.println("Access Token: " + input[1]);
	        }
	        if (input[0].equalsIgnoreCase("accesstokensecret")) {
	            twitterConfigBuilder.setOAuthAccessTokenSecret(input[1]);
	            System.out.println("Access Token Secret: " + input[1]);
	        }
	    }
	    
	    Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
	    twitterConnections.add(twitter);
	}
	
	private Twitter getTwitterConnection(String endPoint) {
		for (Twitter tc : twitterConnections) {
		    try {
		    	Map<String, RateLimitStatus> status = tc.getRateLimitStatus();
                if (status != null) {
                    if (status.containsKey(endPoint)) {
                        if (status.get(endPoint) != null) {
                            
                            if (status.get(endPoint).getRemaining() > 1) {
                            	System.out.println("tc endpoint: " + endPoint + "\t\trate: "+status.get(endPoint).getRemaining());
                            	System.out.println("Working Consumer Key: " + tc.getConfiguration().getOAuthConsumerKey() + " with Consumer Secret: " + tc.getConfiguration().getOAuthConsumerSecret());

                                return tc;
                            }
                        }
                    }
                }
            } catch (TwitterException e) {
            	//System.out.println("Invalid Consumer Key: " + tc.getConfiguration().getOAuthConsumerKey() + " with Consumer Secret: " + tc.getConfiguration().getOAuthConsumerSecret());
                //e.printStackTrace();
            }
        }
		return null;
	}

	public int getFriendTimer(Twitter twitter) {
		try {
			RateLimitStatus status = twitter.getRateLimitStatus().get("/friends/list");
			System.out.println("-----getFriends-----\nLimit: " + status.getLimit());
			System.out.println("Remaining: " + status.getRemaining());
			System.out.println("Seconds Until Reset: " + status.getSecondsUntilReset());
			
			return status.getSecondsUntilReset();
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int getFriendCallsRemaining(Twitter twitter) {
		RateLimitStatus status;
		try {
			status = twitter.getRateLimitStatus().get("/friends/list");
			return status.getRemaining();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
		
	}
	
	
	public void createSyntheticHistoryForUser(String username) {

		//Initialize variables
		userTweets = new Hashtable<String, ArrayList<Status>>();
		friendsOfFriend = new Hashtable<String, ArrayList<User>>();
		ArrayList<URLEntity> randomFriendLinks = new ArrayList<URLEntity>();
		ArrayList<String> selectedFriends = new ArrayList<String>();
		ArrayList<URLEntity> randomFriendOfFriendLinks = new ArrayList<URLEntity>();
		ArrayList<String> selectedFriendsOfFriends = new ArrayList<String>();
		
		int randomLinksCount = 100;
		int randomFriendOfFriendLinksCount = 20;
		try {
			//-------------------PART 1: Friend Links-------------------//
			ArrayList<User> listFriends = getFriendsForUser(username, false);
			randomFriendLinks = getRandomFriendLinks(listFriends, selectedFriends, randomLinksCount);


			//-------------------PART 2: Friend-of-Friend Links-------------------//
			randomFriendOfFriendLinks = getRandomFriendOfFriendLinks(listFriends, selectedFriendsOfFriends, randomFriendOfFriendLinksCount);
			
			
			//-------------------PART 3: Combine Links to create Synthetic History-------------------//
			
			//Mix the friend and friend-of-friend URLs to create a collection of synthetic histories for each user with various sizes and link compositions.
			
			//Make several synthetic histories for one user
			for (int i = 0; i < 3; i++) {
				int size = 50;
				
				switch(i) {
				case 0:
					size = 30;
					break;
				case 1:
					size = 50;
					break;
				case 2:
					size = 100;
					break;
				default: 
					size = 30;
					break;
				}
				
				/*
				ArrayList<URLEntity> history = new ArrayList<URLEntity>();
				
				ArrayList<URLEntity> randomFriendLinksCopy = (ArrayList<URLEntity>) randomFriendLinks.clone();
				ArrayList<URLEntity> randomFriendOfFriendLinksCopy = (ArrayList<URLEntity>) randomFriendOfFriendLinks.clone();
				
				//Add links
				for (int j = 0; j < size; j++) {
					
					
					int chance = randomIntBetween(1,10);
					if (chance <= 8) { //80% chance to take a link from the random friend links array
						if (randomFriendLinksCopy.size() == 0) continue; //TODO: Correct duplicate handling
						int randomInt = randomIntBetween(0, randomFriendLinksCopy.size()-1);
						
						history.add(randomFriendLinksCopy.get(randomInt));
						randomFriendLinksCopy.remove(randomInt);
						
					} else { //20% chance to take a link from the random friend-of-friend links array
						if (randomFriendOfFriendLinksCopy.size() == 0) continue; //TODO: Correct duplicate handling
						int randomInt = randomIntBetween(0,randomFriendOfFriendLinksCopy.size()-1);
						
						history.add(randomFriendOfFriendLinksCopy.get(randomInt));
						randomFriendOfFriendLinksCopy.remove(randomInt);
					} 
				}
				
				
				System.out.println("--------------------------------------------SYNTHETIC HISTORY:--------------------------------------------");
				for (URLEntity url : history) {
					System.out.println(url.getURL());
				}
				
				//Write out the history to a file.
				writeHistoryToFile(history, username+"_"+history.size()+".txt");
				*/
				
				
				JSONObject syntheticHistory = new JSONObject();
				JSONArray friends = new JSONArray();
				
				for (User friend : listFriends) {
					friends.add(friend.getScreenName());
				}
				syntheticHistory.put("friends", friends);
				syntheticHistory.put("numFriends", friends.size());
				
				JSONArray links = new JSONArray();
				
				ArrayList<URLEntity> randomFriendLinksCopy = (ArrayList<URLEntity>) randomFriendLinks.clone();
				ArrayList<String> selectedFriendsCopy = (ArrayList<String>) selectedFriends.clone();
				ArrayList<URLEntity> randomFriendOfFriendLinksCopy = (ArrayList<URLEntity>) randomFriendOfFriendLinks.clone();
				ArrayList<String> selectedFriendsOfFriendsCopy = (ArrayList<String>) selectedFriendsOfFriends.clone();
				
				//Add links
				for (int j = 0; j < size; j++) {
					
					
					int chance = randomIntBetween(1,10);
					if (chance <= 8) { //80% chance to take a link from the random friend links array
						if (randomFriendLinksCopy.size() == 0) continue; //TODO: Correct duplicate handling
						int randomInt = randomIntBetween(0, randomFriendLinksCopy.size()-1);
						
						JSONObject linkPair = new JSONObject();
						linkPair.put("link", randomFriendLinksCopy.get(randomInt).getURL());
						linkPair.put("friend", selectedFriendsCopy.get(randomInt));
						linkPair.put("type", "friend");
						links.add(linkPair);
						
						randomFriendLinksCopy.remove(randomInt);
						selectedFriendsCopy.remove(randomInt);
						
					} else { //20% chance to take a link from the random friend-of-friend links array
						if (randomFriendOfFriendLinksCopy.size() == 0) continue; //TODO: Correct duplicate handling
						int randomInt = randomIntBetween(0,randomFriendOfFriendLinksCopy.size()-1);
						
						JSONObject linkPair = new JSONObject();
						linkPair.put("link", randomFriendOfFriendLinksCopy.get(randomInt).getURL());
						linkPair.put("friend", selectedFriendsOfFriendsCopy.get(randomInt));
						linkPair.put("type", "friend-of-friend");
						links.add(linkPair);
						
						randomFriendOfFriendLinksCopy.remove(randomInt);
						selectedFriendsOfFriendsCopy.remove(randomInt);
					} 
				}
				
				syntheticHistory.put("url-links", links);
				
				System.out.println("--------------------------------------------SYNTHETIC HISTORY:--------------------------------------------");
				for (int z = 0; z < links.size(); z++) {
					System.out.println(((JSONObject) links.get(z)).get("link"));
				}
				
				writeOutJsonObject(syntheticHistory, username+"_"+links.size()+".txt");
			}
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			System.out.println("--------------------------------------------ERROR OCCURED WHEN CREATIN SYNTHETIC HISTORYv --------------------------------------------");
			System.out.println("--------------------------------------------Collected Friend Links:--------------------------------------------");
			for (URLEntity url : randomFriendLinks) {
				System.out.println(url.getURL());
			}
			
			System.out.println("--------------------------------------------Collected Friend-of-Friend Links:--------------------------------------------");
			for (URLEntity url : randomFriendOfFriendLinks) {
				System.out.println(url.getURL());
			}
			
			System.out.println("\t GET FRIENDS CALL COUNT: " + getFriendsCallCount);
			System.out.println("\t GET TIMELINE CALL COUNT: " + userTimelineCallCount);
			
			//System.out.println("Will retry in 10 seconds...");
			//waitSec(10);
			//createSyntheticHistoryForUser(username);
		}
	}
	
	private ArrayList<URLEntity> getRandomFriendOfFriendLinks(ArrayList<User> listFriends, ArrayList<String> selectedFriendsOfFriends, int numLinks) {
		
		ArrayList<URLEntity> randomFriendOfFriendLinks = new ArrayList<URLEntity>();
		if (listFriends.size() == 0)
			return randomFriendOfFriendLinks;
		
		while (randomFriendOfFriendLinks.size() < numLinks) {
			//Get a random friend
			
			User randomFriend = listFriends.get(randomIntBetween(0, listFriends.size()-1));

			//Get the friends of that random friend 
			ArrayList<User> friendsOfRandomFriend;
			if ((friendsOfRandomFriend = friendsOfFriend.get(randomFriend.getScreenName())) == null) { 
				
//				if (getFriendsCallCount >= 9) { //Fallback to ensure limited use of getFriends() call
//					User[] f = (User[]) friendsOfFriend.values().toArray(a)
//					friendsOfRandomFriend = friendsOfFriend.get(f[randomIntBetween(0,f.length)]);
//				} else {
					System.out.println("Random Friend of Friend Links: " + randomFriendOfFriendLinks.size() + " / " + numLinks);
					try {
						friendsOfRandomFriend = getFriendsForUser(randomFriend.getScreenName(), true);
						friendsOfFriend.put(randomFriend.getScreenName(), friendsOfRandomFriend);
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						System.out.println("\nERROR OCCURED GETTING FRIENDS FOR USER: " + randomFriend.getScreenName());
						e.printStackTrace();
						continue;
					}
					
//				}
			}
			
			//If the random friend has friends
			if (friendsOfRandomFriend.size() > 0) {
				
				URLEntity link = null;
				
				while (link == null && friendsOfRandomFriend.size() > 0) {
					//Get the a random friend-of-friend
					User randomFriendOfFriend = friendsOfRandomFriend.get(randomIntBetween(0,friendsOfRandomFriend.size()-1));
		
					//Get a link from the friend-of-friend
					link = getRandomURLLinkForUser(randomFriendOfFriend.getScreenName(), randomFriendOfFriendLinks);
					
					if (link != null) { //If the link is not null, add it to the list
						if (!randomFriendOfFriendLinks.contains(link)) {
							randomFriendOfFriendLinks.add(link);
							selectedFriendsOfFriends.add(randomFriendOfFriend.getScreenName());
						}
						
						
					} else {
						friendsOfRandomFriend.remove(randomFriendOfFriend);
					}
				} //End of while loop
				
				
			} // End of if statement to check if the random friend has friends
			
		} //End of while loop to check if the number of friend-of-friend links has been reached.

		return randomFriendOfFriendLinks;
	}


	private ArrayList<URLEntity> getRandomFriendLinks(ArrayList<User> listFriends, ArrayList<String> selectedFriends, int numLinks) throws TwitterException {

		//Get numLinks random friend links
		ArrayList<URLEntity> randomFriendLinks = new ArrayList<URLEntity>();

		while (randomFriendLinks.size() < numLinks && listFriends.size() != 0) {
			//Get a random friend. 
			User randomFriend = listFriends.get(randomIntBetween(0, listFriends.size()-1));

			//Get a random link from that friend
			URLEntity link = getRandomURLLinkForUser(randomFriend.getScreenName(), randomFriendLinks);
			if (link != null) {

				//Add it to the list of links
				randomFriendLinks.add(link);
				selectedFriends.add(randomFriend.getScreenName());


			} else {
				/////TODO: Currently allowing duplicates links???

				//TODO: Remove user from the list because there are no links for that user
				listFriends.remove(randomFriend);
			}


		}

		//////DEBUG TEST
		System.out.println("\n-------------------------------------- COMPILED RANDOM FRIEND LINKS--------------------------------------");
		for (int i = 0; i < randomFriendLinks.size(); i++) {
			System.out.println(selectedFriends.get(i) + " :\t\t" + randomFriendLinks.get(i).getDisplayURL());
		}
		System.out.println("\n");
		

		return randomFriendLinks;
	}

	/**
	 * Gets a random URL link for the specified user in the past 30 days.
	 * @param username The username of the user
	 * @param currentLinkList The arraylist of links already added.
	 * @return
	 */
	private URLEntity getRandomURLLinkForUser(String username, ArrayList<URLEntity> currentLinkList) {
		
		ArrayList<Status> tweets;
		
		if ((tweets = userTweets.get(username)) == null) {
			//Compile a list of all tweets for user.
			try {
				tweets = getTweetsForUserInPast30Days(username);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				
				System.out.println("\nERROR OCCURED WHILE GETTING URL LINK FOR USER: " + username);
				e.printStackTrace();
				return null;
			}
			
			//Filter tweets to contain URL links
			tweets.removeIf(tweet -> tweet.getURLEntities().length == 0);
			
			//Store for possible later use
			userTweets.put(username, tweets);
		}


		//Get a random URL Link from the last 30 days //( Status class 'getCreatedAt()' returns date. 'getURLEntities' to check if there is a URL attached to the tweet.)
		if (tweets.size() > 0) {
			
			URLEntity url = null;

			Status randomTweet = tweets.get(randomIntBetween(0,tweets.size()-1));
			URLEntity[] urls = randomTweet.getURLEntities();
			url =  urls[randomIntBetween(0,urls.length-1)];
			System.out.println("Found URL:\t" + url.getDisplayURL() + "\t(" + url.getURL() + ")" + "\t\tUser: " + username);
			return url;
		}

		System.out.println("ERROR: User [" + username + "] has not posted a link in the past 3000 tweets or 30 days");
		return null;
	}

	/**
	 * Gets the friends for a user.
	 * @param username Username of the user that you are trying to find the friends of
	 * @return ArrayList<User> The list of friends for the user specified
	 * @throws TwitterException
	 */
	private ArrayList<User> getFriendsForUser(String username, boolean limited) throws TwitterException {

		ArrayList<User> listFriends = new ArrayList<User>();
		//ArrayList<User> listFollowers = new ArrayList<User>();


		int repeatCount = 1; //Due to rate limiting issues, only 200 random friends will be grabbed instead of all the friends if limited == true
		 
		long cursor = -1;
		PagableResponseList<User> pagableFriends;
		do { 
			
			Twitter tc = getTwitterConnection("/friends/list");
			while (tc == null) {
				waitSec(60);
				tc = getTwitterConnection("/friends/list");
			}
			
			pagableFriends = tc.getFriendsList(username, cursor, 200);
			getFriendsCallCount++;
			
			for (User user : pagableFriends) {
				listFriends.add(user); // ArrayList<User>
				//System.out.println(user.getScreenName());
			}
			
			repeatCount = repeatCount - 1;
			
		} while ((cursor = pagableFriends.getNextCursor()) != 0 && (repeatCount > 0 || !limited));

		if (limited) {
			System.out.println("Successfully found " + listFriends.size() + " (max 200) " + " friends for user: " + username);
		} else {
			System.out.println("Successfully found " + listFriends.size() + " friends for user: " + username);
		}

		return listFriends;
	}

	/**
	 * Retrieves the tweets for a user in past 30 days with a maximum of 3000 Tweets. 
	 * @param username
	 * @return
	 * @throws TwitterException
	 */
	private ArrayList<Status> getTweetsForUserInPast30Days(String username) throws TwitterException {
		ArrayList<Status> tweets = new ArrayList<Status>();
		ResponseList<Status> statuses;
		Paging paging = new Paging(1, 100); //100 tweets per page


		Date today = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(today);
		calendar.add(Calendar.DAY_OF_MONTH, -30);
		Date thirtyDaysPrior = calendar.getTime();
		boolean exceeded30Days = false;
		int repeatCount = 30; //Retrieves a maximum of 3000 tweets
		
		do {
			//Get all the tweets for the user
			Twitter tc = getTwitterConnection("/statuses/user_timeline");
			while (tc == null) {
				waitSec(60);
				tc = getTwitterConnection("/statuses/user_timeline");
			}
			
			statuses = tc.getUserTimeline(username, paging);
			userTimelineCallCount++;
			
			for (Status status : statuses) {

				//Assuming the tweets are in chronological order, stop adding the tweets to the array once it exceeds 30 days
				if (!status.getCreatedAt().before(thirtyDaysPrior)) {
					tweets.add(status);
				} else {
					exceeded30Days = true;
					break;
				}

				//Increment page
				paging.setPage(paging.getPage()+1);
				repeatCount--;
			}
		} while (statuses.size() != 0 && repeatCount > 0 && exceeded30Days == false); 

		System.out.println("Successfully found " + tweets.size() + " (max 3000) tweets for user: " + username);
		
		return tweets;
	}

	/**
	 * Helper function to retrieve a random integer between min and max inclusive.
	 * @param min
	 * @param max
	 * @return
	 */
	private int randomIntBetween(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - min + 1) + min;
	}
	
	/**
	 * Helper function to wait some period of time in milliseconds
	 * @param time
	 */
	private void waitSec(int time) {
		System.out.println("SLEEPING...");
		
		int timeInterval = 60;
		int sleepTime = timeInterval;
		
		do {
			System.out.println("Time remaining: " + time);
			
			sleepTime = timeInterval;
			time = time - timeInterval;
			if (time < 0) {
				sleepTime+=time;
			}

			try {
				TimeUnit.SECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} while (time > 0); 
		
	}
	
	/**
	 * Helper function used to write out a synthetic history to a file.
	 * @param history ArrayList of URLEntity. (URLs)
	 * @param filename Name of txt file
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeHistoryToFile(ArrayList<URLEntity> history, String filename) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File("\\synthetic_histories\\");
		if (!file.exists()) {
			file.mkdir();
			System.out.println("Created new synthetic_histories folder");
		}
		
		//try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"))) {
		try (Writer writer = new BufferedWriter(new FileWriter(new File("\\synthetic_histories\\"+filename)))) {
			//-------------------- Print synthetic history		
			writer.write("--------------------------------------------SYNTHETIC HISTORY:--------------------------------------------\n");
			writer.write("Size: " + history.size()+"\n\n");
			for (URLEntity url : history) {
				writer.write(url.getURL() + "\n");
			}
		}
	}
	
	public void writeOutFriendsListForUser(String user) {
		
		ArrayList<User> listFriends = null ;
		try {
			listFriends = getFriendsForUser(user, false);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.out.println("Will retry write out operation in 10 seconds");
			waitSec(10);
			writeOutFriendsListForUser(user);
		}
		
		
		File file = new File("\\synthetic_histories\\");
		if (!file.exists()) {
			file.mkdir();
			System.out.println("Created new synthetic_histories folder");
		}
		
		//try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"))) {
		try (Writer writer = new BufferedWriter(new FileWriter(new File("\\synthetic_histories\\"+user+"_friends.txt")))) {
			//-------------------- Print synthetic history		
			writer.write("--------------------------------------------FRIENDS:--------------------------------------------\n");
			writer.write("Size: " + listFriends.size()+"\n\n");
			for (User u : listFriends) {
				writer.write(u.getScreenName() + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeOutJsonObject(JSONObject obj, String filename) {
		File file = new File("\\synthetic_histories\\");
		if (!file.exists()) {
			file.mkdir();
			System.out.println("Created new synthetic_histories folder");
		}
		
		try (Writer writer = new BufferedWriter(new FileWriter(new File("\\synthetic_histories\\"+filename)))) {	
			writer.write(JsonWriter.formatJson(obj.toJSONString()));
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
