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
 
public class SearchTweets {

        private final static String CONSUMER_KEY        = "myKey";
        private final static String CONSUMER_KEY_SECRET = "mySecret";
        public static Twitter twitter = new TwitterFactory().getInstance();
 
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
       

        public static void checkArgs(String[] args) {
                if (args.length < 1) {
                        System.out.println("java twitter4j.examples.search.SearchTweets [query]");
                        System.exit(-1);
                }
        } //end checkArgs
       
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
       
        public static File createFile(String filename) throws IOException {
                File file = new File(filename);
                file.createNewFile();
               
                return file;
        } //end createFile
 
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
       
        public static boolean isSong(String tweet) {
                return !(tweet.charAt(0) == '@' ||
                         tweet.charAt(0) == '#' ||
                         tweet.substring(0, 6).equals("Follow") ||
                         tweet.substring(0, 6).equals("follow"));
        } //end isSong 
 
        public static String cleanTweet(Status tweet) {                
                return tweet.getText().substring(0, tweet.getText().lastIndexOf(" playing on"));
        } //end cleanTweet
        
        public static void printNewSongs(Set<String> set, int numNewSongs) {
        	String[] setToArray = set.toArray(new String[set.size()]);
        	for(int i = setToArray.length-numNewSongs; i < set.size(); i++) {
        		System.out.println(setToArray[i]);
        	}
        } //end printNewSongs
        
            	public static ArrayList<String> cleanExistingFile(ArrayList<String> al) {
    		ArrayList<String> result = new ArrayList<String>();
    		for (int i = 0; i < al.size(); i++) {
    			String s = al.get(i);
    			if (!s.contains("http")       && 
    				!s.contains("Like It")    && 
    				!s.contains("play along") && 
    				!s.contains("Ur Edm Bae")) {
    				
    				if (s.indexOf("#bpmBreaker") > 0 || s.indexOf("#BpmBreaker") > 0) {
    					result.add(s.substring(0, s.indexOf("#") - 1));
    				} else {
    					result.add(s);
    				}
    			}
    		}

    		return result;
    	} //end cleanExisitngFile

}
