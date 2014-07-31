import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
 
/**
 * Searches for Tweets from a twitter account and parses results into a file<br><br>
 *
 * Major outline of this program can be attributed to RDeJourney from URL:<br>
 * http://namingexception.wordpress.com/2011/09/12/how-easy-to-make-your-own-twitter-client-using-java/<br><br>
 *
 * Program argument containing Twitter handle must be given prior to run time! e.g. "bmp_playlist" no quotes<br><br>
 *
 * The rest of the program is written by @author Blake Runkle
 *
 * @author Blake Runkle
 */
public class SearchTweets {
        /**
         * Usage: java twitter4j.examples.search.SearchTweets [query]
         *
         * @param args search query
         */
        private final static String CONSUMER_KEY = "eMZTgNkkgi37aZmNOXeHPbDR1";
        private final static String CONSUMER_KEY_SECRET = "yLoTulZJOdEUXOrFD52ttnITyBi5ZU6YftZnoCNGICJbqb0Oym";
        //public static final String DATE_PATTERN = "(\\w{3}\\s){2}\\d{2}\\s(\\d{2}\\W){2}\\d{2}\\s\\w{3}\\s\\d{4}";
        //public static final int TIME_STAMP_LENGTH = 28;
        //public static final String DATE_FORMAT = "EEE MMM dd hh:mm:ss zzz yyyy";
        public static Twitter twitter = new TwitterFactory().getInstance();
 
        /**
         * Queries and stores songs from @bmp_playlist
         *
         * @param args Twitter handle to be queried
         * @throws TwitterException
         * @throws IOException
         */
        public static void main(String[] args) throws TwitterException, IOException {
                checkArgs(args);               
                authenticateProgram(CONSUMER_KEY, CONSUMER_KEY_SECRET);
               
                ArrayList<String> initialSongList = new ArrayList<String>();
                File bmp_playlistFile = createFile("bmp_playlist.txt");
                initialSongList = readFileIntoArrayList("bmp_playlist.txt", bmp_playlistFile, initialSongList);
                
                int numSongsBefore = initialSongList.size();
                
                PrintWriter pw = new PrintWriter(new FileWriter(bmp_playlistFile));
               
                try {
                        Query query = new Query(args[0]);
                        QueryResult result;
                        do {                           
                                result = twitter.search(query);
                                List<Status> tweets = result.getTweets();
                                for (Status tweet : tweets) {
                                        if (tweet.getUser().getScreenName().equals("bpm_playlist")) {                                          
                                                String songAndArtist = cleanTweet(tweet);                                              
                                                if(isSong(songAndArtist) && !initialSongList.contains(songAndArtist)) {
                                                        initialSongList.add(songAndArtist);
                                                }
                                        }
                                }                              
                        } while ((query = result.nextQuery()) != null);
                       
                        //converting ArrayList to LinkedHashSet removes any duplicate Strings
                        Set<String> duplicateFreeSet = new LinkedHashSet<>(initialSongList);
                        
                        int numSongsAfter = duplicateFreeSet.size();
                        int numNewSongs = numSongsAfter - numSongsBefore;
                       
                        //Write the contents of duplicateFreeSet "bmp_playlist.txt"
                        for(String song : duplicateFreeSet) {                               
                                pw.println(song);
                        }
                        
                        System.out.println("Num songs before: " + numSongsBefore);
                        System.out.println("Num songs after: " + numSongsAfter);
                        System.out.println("New songs added: " + numNewSongs);
                        printNewSongs(duplicateFreeSet, numNewSongs);
                       
                } catch (TwitterException te) {
                        te.printStackTrace();
                        System.out.println("Failed to search tweets: " + te.getMessage());
                        System.exit(-1);
                } finally {
                        try {
                                pw.close();
                        } catch (Exception ex) {}
                }
               
                System.exit(0);
        } //end main
       
        /**
         * Check if program arguments exist before executing any further statements
         * @param args Program arguments passed to "main" via command line or "Run Configurations.."
         */
        public static void checkArgs(String[] args) {
                if (args.length < 1) {
                        System.out.println("java twitter4j.examples.search.SearchTweets [query]");
                        System.exit(-1);
                }
        } //end checkArgs
       
        /**
         * Authenticates user credentials by applying unique key and secretKey. Will ask for
         * one time PIN number from generated URL.
         *
         * @param consumerKey Unique key for Twitter account /@runklebc
         * @param consumerKeySecret Unique keySecret for Twitter account /@runklebc
         * @throws TwitterException
         * @throws IOException
         */
        public static void authenticateProgram(String consumerKey, String consumerKeySecret) throws TwitterException, IOException {
                twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
                RequestToken requestToken = twitter.getOAuthRequestToken();
                System.out.println("Authorization URL: \n" + requestToken.getAuthenticationURL());
 
                AccessToken accessToken = null;
 
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (accessToken == null) {
                        try {
                                System.out.print("Input PIN here: ");
                                String pin = br.readLine();
 
                                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } catch (TwitterException te) {
                                System.out.println("Failed to get access token, caused by: " + te.getMessage());
                                System.out.println("Retry input PIN");
                        }
                }
 
                System.out.println("Access Token: " + accessToken.getToken());
                System.out.println("Access Token Secret: " + accessToken.getTokenSecret());
        } //end authenticateProgram    
       
        /**
         * Creates a file inside the current project
         * @param filename Desired name of file
         * @return File object
         * @throws IOException
         */
        public static File createFile(String filename) throws IOException {
                File file = new File(filename);
                file.createNewFile();
               
                return file;
        }
 
        /**
         * Reads existing file contents into an ArrayList to maintain old content.
         * Deletes existing file then recreates it with the same name to ensure
         * duplicate items are not written to the file.
         *  
         * @param filename Name of file represented as a String
         * @param f File to be deleted then recreated
         * @param initialSongList ArrayList to store contents of file
         * @return ArrayList of type String containing contents of previous file
         * @throws IOException
         */
        public static ArrayList<String> readFileIntoArrayList(String filename, File f, ArrayList<String> pArrayList) throws IOException {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                   pArrayList.add(line);
                }
                br.close();
                f.delete();
                f.createNewFile();               
               
                return pArrayList;
        } //end readContentsOfFileIntoInitialSongList
       
        /**
         * Verifies that a Tweet is a song/artist combo.
         * @param tweet Tweet to be checked
         * @return True if Tweet is a song, false otherwise.
         */
        public static boolean isSong(String tweet) {
                return !(tweet.charAt(0) == '@' ||
                         tweet.charAt(0) == '#' ||
                         tweet.substring(0, 6).equals("Follow") ||
                         tweet.substring(0, 6).equals("follow"));
        } //end isSong 
 
        /**
         * Remove all text from Tweet that is not song/artist combo
         * @param tweet Tweet to be cleaned
         * @return String containing only song/artist combo
         */
        public static String cleanTweet(Status tweet) {                
                return tweet.getText().substring(0, tweet.getText().lastIndexOf(" playing on"));
        } //end cleanTweet
        
        /**
         * Prints new songs added to file
         * @param set Set containing items to be printed
         * @param numNewSongs Number of songs to be printed
         */
        public static void printNewSongs(Set<String> set, int numNewSongs) {
        	String[] setToArray = set.toArray(new String[set.size()]);
        	for(int i = setToArray.length-numNewSongs; i < set.size(); i++) {
        		System.out.println(setToArray[i]);
        	}
        } //end printNewSongs
       
}
