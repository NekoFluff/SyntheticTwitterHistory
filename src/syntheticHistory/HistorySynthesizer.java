package syntheticHistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class HistorySynthesizer {
	
	int userTimelineCallCount = 0;
	int getFriendsCallCount = 0;

	Hashtable<String, ArrayList<Status>> userTweets = new Hashtable<String, ArrayList<Status>>();
	Hashtable<String, ArrayList<User>> friendsOfFriend = new Hashtable<String, ArrayList<User>>();
	
	// The factory instance is re-useable and thread safe.
	//Twitter twitter = TwitterFactory.getSingleton();

	private Set<Twitter> twitterConnections;
	
	public HistorySynthesizer() {
		twitterConnections = new HashSet<Twitter>();
		twitterConnections.add(TwitterFactory.getSingleton());
		
		initTwitterConnections("config2.txt");
	}

	public static void main(String[] args) {

		HistorySynthesizer x = new HistorySynthesizer();
		x.doStuff();

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
                            System.out.println("tc endpoint: " + endPoint + "\t\trate: "+status.get(endPoint).getRemaining());
                            if (status.get(endPoint).getRemaining() > 3) {
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
	
	public void doStuff() {
		String username = "Leanne";//"untappd";



		/*
		String[] args2 = {username};
		//args = {"Alex Nou"};

		if (args2.length < 1) {
			System.out.println(
					"Usage: java twitter4j.examples.user.LookupUsers [screen name[,screen name..]]");
			System.exit(-1);
		}
		try {
			//Twitter twitter = new TwitterFactory().getInstance();
			ResponseList<User> users = twitter.lookupUsers(args2[0].split(","));
			for (User user : users) {
				if (user.getStatus() != null) {
					System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
					System.out.println("# of Friends: " + user.getFriendsCount());

				} else {
					// the user is protected
					System.out.println("@" + user.getScreenName());
					System.out.println("# of Friends: " + user.getFriendsCount());
				}
			}
			System.out.println("Successfully looked up users [" + args2[0] + "].");
			//System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to lookup users: " + te.getMessage());
			System.exit(-1);
		}
		 */

		ArrayList<URLEntity> randomFriendLinks = new ArrayList<URLEntity>();
		ArrayList<URLEntity> randomFriendOfFriendLinks = new ArrayList<URLEntity>();
		int randomLinksCount = 220;
		int randomFriendOfFriendLinksCount = 39;
		try {
			//-------------------PART 1: Friend Links-------------------//
			ArrayList<User> listFriends = getFriendsForUser(username);
			randomFriendLinks = getRandomFriendLinks(listFriends, randomLinksCount);


			//-------------------PART 2: Friend-of-Friend Links-------------------//
			randomFriendOfFriendLinks = getRandomFriendOfFriendLinks(listFriends, randomFriendOfFriendLinksCount);
			
			
			//TODO: Write friend links to file and friend-of-friend links to file
			
			
			//-------------------PART 3: Combine Links to create Synthetic History-------------------//
			
			//blend the friend and friend-of-friend URLs to create a collection of synthetic histories 
			//for each user with various sizes and link compositions.
			
			//Make 5 possible histories
			for (int i = 0; i < 5; i++) {
				
				//Choose between 10 and 50 links to be in the synthetic history
				int size = 200;//randomIntBetween(20,50);
			
				ArrayList<URLEntity> history = new ArrayList<URLEntity>();
				
				//Add links
				for (int j = 0; j < size; j++) {
					
					int chance = randomIntBetween(1,10);
					if (chance <= 8) { //80% chance to take a link from the random friend links array
						history.add(randomFriendLinks.get(randomIntBetween(0,randomFriendLinks.size()-1)));
					} else { //20% chance to take a link from the random friend-of-friend links array
						history.add(randomFriendOfFriendLinks.get(randomIntBetween(0,randomFriendOfFriendLinks.size()-1)));
					} //TODO: CURRENTLY ALLOWS DUPLICATES IN THE HISTORY. DOES THIS REQUIRE A CHANGE?
				}
				
				
				//TODO: Write out history


				
				System.out.println("--------------------------------------------SYNTHETIC HISTORY:--------------------------------------------");
				for (URLEntity url : history) {
					System.out.println(url.getURL());
				}
				
				writeHistoryToFile(history, username+"_200_"+(i+1)+".txt");
			}
			

			
			
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			
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
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<URLEntity> getRandomFriendOfFriendLinks(ArrayList<User> listFriends, int numLinks) {
		
		ArrayList<URLEntity> randomFriendOfFriendLinks = new ArrayList<URLEntity>();
		
		while (randomFriendOfFriendLinks.size() < numLinks) {
			//Get a random friend
			User randomFriend = listFriends.get(randomIntBetween(0, listFriends.size()-1));

			//Get the friends of that random friend TODO: Store this in a hashtable
			ArrayList<User> friendsOfRandomFriend;
			if ((friendsOfRandomFriend = friendsOfFriend.get(randomFriend.getScreenName())) == null) { 
				
//				if (getFriendsCallCount >= 9) { //Fallback to ensure limited use of getFriends() call
//					User[] f = (User[]) friendsOfFriend.values().toArray(a)
//					friendsOfRandomFriend = friendsOfFriend.get(f[randomIntBetween(0,f.length)]);
//				} else {
					System.out.println("Random Friend of Friend Links: " + randomFriendOfFriendLinks.size() + " / " + numLinks);
					try {
						friendsOfRandomFriend = getFriendsForUser(randomFriend.getScreenName());
						friendsOfFriend.put(randomFriend.getScreenName(), friendsOfRandomFriend);
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						}
						
					} else {
						friendsOfRandomFriend.remove(randomFriendOfFriend);
					}
				} //End of while loop
				
				
			} // End of if statement to check if the random friend has friends
			
		} //End of while loop to check if the number of friend-of-friend links has been reached.

		return randomFriendOfFriendLinks;
	}


	private ArrayList<URLEntity> getRandomFriendLinks(ArrayList<User> listFriends, int numLinks) throws TwitterException {

		//Get numLinks random friend links
		ArrayList<URLEntity> randomFriendLinks = new ArrayList<URLEntity>();
		ArrayList<String> randomFriends = new ArrayList<String>(); //Names TODO: REMOVE


		while (randomFriendLinks.size() < numLinks && listFriends.size() != 0) {
			//Get a random friend. 
			User randomFriend = listFriends.get(randomIntBetween(0, listFriends.size()-1));

			//Get a random link from that friend
			URLEntity link = getRandomURLLinkForUser(randomFriend.getScreenName(), randomFriendLinks);
			if (link != null) {

				//Add it to the list of links
				randomFriendLinks.add(link);
				randomFriends.add(randomFriend.getScreenName());


			} else {
				/////TODO: Currently allowing duplicates links???

				//TODO: Remove user from the list because there are no links for that user
				listFriends.remove(randomFriend);
			}


		}

		//////DEBUG TEST
		System.out.println("\n-------------------------------------- COMPILED RANDOM FRIEND LINKS--------------------------------------");
		for (int i = 0; i < randomFriendLinks.size(); i++) {
			System.out.println(randomFriends.get(i) + " :\t\t" + randomFriendLinks.get(i).getDisplayURL());
		}
		System.out.println("\n");

		return randomFriendLinks;
	}

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


	private ArrayList<User> getFriendsForUser(String username) throws TwitterException {


		ArrayList<User> listFriends = new ArrayList<User>();
		//ArrayList<User> listFollowers = new ArrayList<User>();

		Twitter tc = getTwitterConnection("/friends/list");
		while (tc == null) {
			waitSec(60);
			tc = getTwitterConnection("/friends/list");
		}
		int repeatCount = 5;
		
		long cursor = -1;
		PagableResponseList<User> pagableFriends;
		do { //Due to rate limiting issues, only 1000 random friends will be grabbed instead of all the friends.
			pagableFriends = tc.getFriendsList(username, cursor, 200);
			getFriendsCallCount++;
			
			for (User user : pagableFriends) {
				listFriends.add(user); // ArrayList<User>
				//System.out.println(user.getScreenName());
			}
			repeatCount--;
		} while ((cursor = pagableFriends.getNextCursor()) != 0 && repeatCount > 0);

		// get followers
		//	        cursor = -1;
		//	        PagableResponseList<User> pagableFollowers;
		//	        do {
		//	            pagableFollowers = twitter.getFollowersList(twitter.getId(), cursor);
		//	            for (User user : pagableFollowers) {
		//	                listFollowers.add(user); // ArrayList<User>
		//	            }
		//	        } while ((cursor = pagableFollowers.getNextCursor()) != 0);
		//	
		System.out.println("Successfully found 200 friends for user: " + username);


		return listFriends;
	}

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

		return tweets;
	}

	private int randomIntBetween(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - min + 1) + min;
	}
	
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
	
	public void writeHistoryToFile(ArrayList<URLEntity> history, String filename) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"))) {
			
			//-------------------- Print synthetic history		
			writer.write("--------------------------------------------SYNTHETIC HISTORY:--------------------------------------------\n");
			writer.write("Size: " + history.size()+"\n\n");
			for (URLEntity url : history) {
				writer.write(url.getURL() + "\n");
			}
		}
	}
}
