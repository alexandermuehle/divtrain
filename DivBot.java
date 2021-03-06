import org.jibble.pircbot.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*; 
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

public class DivBot extends PircBot {
    
public static String LAST_FM;
		
		//regex for !lookup name
		public static Pattern prof = Pattern.compile("(!profile)\\s*(\\w*)\\s*\\z");
		public static Pattern div = Pattern.compile("(!div)\\s*(\\w*)\\s*\\z");
		public static Pattern ip = Pattern.compile("(!server)\\s*(\\w*)\\s*\\z");
		public static Pattern song = Pattern.compile("(!song)\\s*\\z");
		public static Pattern song2 = Pattern.compile("(!np)\\s*\\z");
		public static Pattern hours = Pattern.compile("(!hours)\\s*(\\w*)\\s*(\\w*)");
		public static Pattern stats = Pattern.compile("(!stats)\\s*(\\w*)\\s*(\\w*)");
		public static Pattern log = Pattern.compile("(!log)\\s*(\\w*)");
		public static Pattern credit = Pattern.compile("(!credit)\\b");
		public static Pattern conn = Pattern.compile("(!connect)\\b");
		public static Pattern comm = Pattern.compile("(!command)");
		public static Pattern toggle = Pattern.compile("(!toggle)\\s*(\\w*)\\s*\\z");
		Matcher m;
		
		public String mods = "";
		
		//permission boolean
		public boolean profB = true;
		public boolean divB = true;
		public boolean hoursB = true;
		public boolean npB = true;
		public boolean statsB = true;
		public boolean serverB = true;
		public boolean logB = true;
		
		
    public DivBot(String lastfm) {
        this.setName("divtrain");
	LAST_FM = lastfm;
    }
    
	//react to private message
	public void onPrivateMessage(String sender, String login, String hostname, String message) 
		mods = message;
		System.out.println("updated mods");
	}
	
	//on connect
	public void onUserList(String channel, User[] users) {
		sendMessage(channel, "/mods");
		System.out.println("getting mods");
	}
	
    // react to messages
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
		

		
		//PROFILE
		m = prof.matcher(message);
		if (m.find()) {
			if ( !profB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (profile)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = m.group(2);	//group 2 = name
			if ( target.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String msg = target + ": http://steamcommunity.com/profiles/" + targetSteam;
				System.out.println(channel + ": http://steamcommunity.com/profiles/" + targetSteam); //LOGGING
				sendMessage(channel, msg);
			}
		}
		
		//LASTFM
		if ( ( song.matcher(message).find()) || ( song2.matcher(message).find())) {
			if ( !npB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (lastfm)"); //LOGGING
				return;
			}
			String songInfo = getSongFromLastFM(LAST_FM);
			String msg = "this should never be seen";
			if (songInfo.equals("private")){
				msg = "No Song playing right now";
			}
			else{
				msg = sender + ": Now playing: " + songInfo;
				System.out.println(channel + ": " +songInfo); //LOGGING
			}
			
			sendMessage(channel, msg);
		}

		
		//STATS
		m = stats.matcher(message);
		if (m.find()) {
			if ( !statsB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (stats)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = m.group(2);	//group 2 = name
			String game = m.group(3);	//group 3 = game
			if ( target.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			if ( !game.equalsIgnoreCase("tf2") && !game.equalsIgnoreCase("csgo") && !game.equalsIgnoreCase("dota2") ){
				sendMessage(channel, sender + ": please specify the game you want stats of as follows: Dota 2 = dota2, TeamFortress 2 = tf2, CS:GO = csgo");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target)){
				if (game.equalsIgnoreCase("tf2")){
					sendMessage(channel, target + ": http://logs.tf/profile/" + targetSteam);
					System.out.println(channel + ": http://logs.tf/profile/" + targetSteam);
				}
			 	if (game.equalsIgnoreCase("dota2")){
					sendMessage(channel, target + ": dotabuff.com/players/" + targetSteam);
					System.out.println(channel + ": dotabuff.com/players/" + targetSteam);
				}
				if (game.equalsIgnoreCase("csgo")){
					sendMessage(channel, target + ": http://csgo-stats.com/" + targetSteam);
					System.out.println(channel + ": http://csgo-stats.com/" + targetSteam);
				}
			}
		}
				
		//LOG
		m = log.matcher(message);
		if (m.find()) {
			if ( !logB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (log)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = m.group(2);	//group 2 = name
			if ( target.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String logLink = getLogLink(targetSteam);
				String msg = target + ": Last game: " + logLink;
				sendMessage(channel, msg);
				System.out.println(channel + ": " + logLink); //LOGGING
			}
		}
		
		//DIV 
		m = div.matcher(message);
		if (m.find()) {
			if ( !divB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (div)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = m.group(2);	//group 2 = name
			if ( target.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String etf2lInfo = getDivFromETF2L(longToSteam(new Long(targetSteam)));
				String msg = target + ": " + etf2lInfo;
				sendMessage(channel, msg);
				System.out.println(channel + ": " + etf2lInfo); //LOGGING
			}
		}
		
		//IP
		m = ip.matcher(message);
		if (m.find()) {
			if ( !serverB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (ip)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = m.group(2);	//group 2 = name
			if ( target.equals("") ){
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String IPInfo = getServerFromSteam(targetSteam);
				String msg = target + ": Server Info: " + IPInfo;
				System.out.println(channel + ": " + IPInfo); //LOGGING
				sendMessage(channel, msg);
			}
		}
		
		//HOURS
		m = hours.matcher(message);
		if ( m.find() ) {
			if ( !hoursB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (hours)"); //LOGGING
				return;
			}
			String target = m.group(2);	
			String game = m.group(3);

			if ( target.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no name provided");
				return;
			}
			if ( !game.equalsIgnoreCase("tf2") && !game.equalsIgnoreCase("csgo") && !game.equalsIgnoreCase("dota2") ){
				sendMessage(channel, sender + ": please specify the game you want stats of as follows: Dota 2 = dota2, TeamFortress 2 = tf2, CS:GO = csgo");
				return;
			}
			
			//getting steam from twitch
			String targetSteam = getSteamFromTwitch(target, channel);
			String targetHours = "No game specified";
			String msg = "";
			String msgGame = "";
			if ( !errorHandling(targetSteam, channel, target) ){
				switch(game.toLowerCase()){
					case "tf2": 
						targetHours = getHoursFromSteam(targetSteam, 440, channel); // second arg is gameId (440=tf2)
						msgGame = "h played in TF2";
						break;
					case "dota2":
						targetHours = getHoursFromSteam(targetSteam, 570, channel); // second arg is gameId (440=tf2)
						msgGame = "h played in Dota";
						break;
					case "csgo":
						targetHours = getHoursFromSteam(targetSteam, 730, channel); // second arg is gameId (440=tf2)
						msgGame = "h played in CS:GO";
						break;
					default:
						sendMessage(channel, sender + ": please specify the game you want hours of as follows: Dota 2 = dota2, TeamFortress 2 = tf2, CS:GO = csgo");
						return;
				}
				if ( targetHours.equals("private") ){
					sendMessage(channel, target + " has set his profile to private or has other privacy settings enabled");
					return;
				}
				else if ( targetHours.equals("steamConnectionError") ){
					sendMessage(channel, target + ": There has been a Steam Connection Error"); 
				}
				else{
					msg = target + ": " + targetHours + msgGame;
					sendMessage(channel, msg);
					System.out.println(channel + ": " + targetHours + msgGame); //LOGGING
				}
			}
		}
		
		//CREDIT
		if (credit.matcher(message).find()) {
			sendMessage(channel, "This bot has been created by lexs. Please feel free to send feedback to: alexander.muhle@gmail.com");
			System.out.println(channel + ": CREDIT"); //LOGGING
		}
		
		//CONNECT 
		if (conn.matcher(message).find()) {
			sendMessage(channel, "Settings->Connections->Social->Steam");
		}
		
		//TOGGLE
		m = toggle.matcher(message);
		if ( m.find() ){
			String command = m.group(2);	//group 2 = command
			if ( command.equalsIgnoreCase("") ) { 
				sendMessage(channel, sender + ": no command provided");
				return;
			}
			else{
				if ( mods.contains(sender) || channel.contains(sender) ){
					switch (command) {
						case "profile":
							profB = !profB;
							if (!profB)
								sendMessage(channel, sender + ": !profile has been disabled");
							if (profB)
								sendMessage(channel, sender + ": !profile has been enable");
							break;
						case "np":
							npB = !npB;
							if (!npB)
								sendMessage(channel, sender + ": !np has been disabled");
							if (npB)
								sendMessage(channel, sender + ": !np has been enable");							
							break;
						case "stats":
							statsB = !statsB;
							if (!statsB)
								sendMessage(channel, sender + ": !stats has been disabled");
							if (statsB)
								sendMessage(channel, sender + ": !stats has been enabled");
							break;
						case "log":
							logB = !logB;
							if (!logB)
								sendMessage(channel, sender + ": !log has been disabled");
							if (logB)
								sendMessage(channel, sender + ": !log has been enabled");
							break;
						case "div":
							divB = !divB;
							if (!divB)
								sendMessage(channel, sender + ": !div has been disabled");
							if (divB)
								sendMessage(channel, sender + ": !div has been enabled");
							break;
						case "server":
							serverB = !serverB;
							if (!serverB)
								sendMessage(channel, sender + ": !server has been disabled");
							if (serverB)
								sendMessage(channel, sender + ": !server has been enabled");
							break;
						case "hours":
							hoursB = !hoursB;
							if (!hoursB)
								sendMessage(channel, sender + ": !hours has been disabled");
							if (hoursB)
								sendMessage(channel, sender + ": !hours has been enabled");	
							break;
					}
				}
				else{
					sendMessage(channel, sender + ": Sorry you are not a mod");
					return;
				}
			}
		}
		
		//COMMANDS 
		if (comm.matcher(message).find()) {
			sendMessage(channel, "Commands: !server [name] || !stats [name] [game] || !hours [name] [game] || !profile [name] || !div [name] || !log [name] || !np || !credit");
		}
	}
	
	private String getLogLink(String targetSteam){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://logs.tf/json_search?player=" + targetSteam + "&limit=1";
		String result = "";		
		String line = "";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = in.readLine()) != null)
				result = result + line;
			in.close();
		}
		catch(Exception e) {
			return "logsTFConnectionError";
		}
		
		//JSON
		JSONObject response = null;
		try{
			JSONObject logObj = new JSONObject(result);
			JSONArray logArray = logObj.getJSONArray("logs");
			JSONObject log = logArray.getJSONObject(0);
			
			return log.getString("title") + "  - logs.tf/" + Integer.toString(log.getInt("id"));
		}
		catch(Exception e){
			e.printStackTrace(System.out);
			return "Error reading JSON from logs.tf";
		}
	}
	
	private String getSongFromLastFM(String lastfm){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=" + LAST_FM + "&limit=1&api_key=d5732c5865e524de715834b800e33d3c&format=json";
		System.out.println(url);
		String result = "";		
		String line = "";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = in.readLine()) != null)
				result = result + line;
			in.close();
		}
		catch(Exception e) {
			return "lastFMConnectionError";
		}

		//JSON
		JSONObject response = null;
		try{
			JSONObject lastFMJson = new JSONObject(result);
			if (lastFMJson.has("error"))
				return "wrongSetup";
			JSONObject recent = lastFMJson.getJSONObject("recenttracks");
			JSONArray trackArray = recent.getJSONArray("track");
			JSONObject track = trackArray.getJSONObject(0);
			JSONObject artist = track.getJSONObject("artist");
			String artistText = artist.getString("#text");
			String name = track.getString("name");
			return artistText + " - " + name;
		}
		catch(Exception e){
			return "private";
		}
	}	

	private String getServerFromSteam(String targetSteam){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=209970D697E121D8CCFB244B336585B9&steamids=" + targetSteam; //default is json
		String result = "";		
		String line = "";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = in.readLine()) != null)
				result = result + line;
			in.close();
		}
		catch(Exception e) {
			return "steamConnectionError";
		}
		
		//JSON
		JSONObject response = null;
		JSONArray players = null;
		try{
			JSONObject steamJson = new JSONObject(result);
			if ( steamJson.has("response") && !steamJson.isNull("response") ){
				response = steamJson.getJSONObject("response");
				if (response.has("players") && !response.isNull("players") ){
					players = response.getJSONArray("players");
					for (int i = 0; i < players.length(); i++){
						JSONObject player = players.getJSONObject(i);
						if (player.getInt("communityvisibilitystate") == 3){
							if(player.has("gameserverip"))
								return "connect " + player.getString("gameserverip");
						}
						else{
							return "private";
						}
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace(System.out);
			return "private";
		}
		return "Not on a server right now";
	}
	
	private String getHoursFromSteam(String targetSteam, int gameId, String channel){
		
		//REQUEST
		URLConnection connection = null;
		String url = "http://api.steampowered.com/IPlayerService/GetRecentlyPlayedGames/v0001/?key=209970D697E121D8CCFB244B336585B9&steamid=" + targetSteam; //default is json
		String result = "";		
		String line = "";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = in.readLine()) != null)
				result = result + line;
			in.close();
		}
		catch(Exception e) {
			return "steamConnectionError";
		}

		//JSON
		try{
			JSONObject steamJson = new JSONObject(result);
			if ( steamJson.has("response") && !steamJson.isNull("response") ){

				JSONObject response = steamJson.getJSONObject("response");
				JSONArray games = response.getJSONArray("games");

				for (int i = 0; i < games.length(); i++){
					JSONObject game = games.getJSONObject(i);
					if ( game.getInt("appid") == gameId){
						int minutes = game.getInt("playtime_forever");
						int hours = minutes / 60;
						return Integer.toString(hours);
					}
				}
			}
			else
				return "noSteamId";
		}
		catch(Exception e){
			e.printStackTrace(System.out);
			return "private";
		}
		return "Error occurred while looking up the hours";
	}
	
	private String getProfileFromBuff(String targetSteam, String channel){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://dotabuff.com/search/hints.json?q=" + targetSteam; //hint json, fuck xml
		String result = "";		
		String line = "";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = in.readLine()) != null)
				result = result + line;
			in.close();
		}
		catch(Exception e) {
			return "steamConnectionError";
		}
		
		//JSON
		try{
			JSONArray buffJsonA = new JSONArray(result);
			JSONObject buffJson = buffJsonA.getJSONObject(0);
			if ( buffJson.has("url") && !buffJson.isNull("url") )
				return buffJson.getString("url");
			else
				return "noBuff";
		}
		catch(Exception e){
			e.printStackTrace();
			return "buffJsonError";
		}
	}

    private String getSteamFromTwitch(String target, String channel){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://api.twitch.tv/api/channels/" + target;
		String result = "error";
		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
									connection.getInputStream()));
									
			result = in.readLine();
			in.close();
		}
		catch(Exception e) {
			return "twitchConnectionError";
		}

		//JSON
		try{
			JSONObject twitchJson = new JSONObject(result);
			if ( twitchJson.has("steam_id") && !twitchJson.isNull("steam_id") )
				return twitchJson.getString("steam_id");
			else
				return "noSteamId";
		}
		catch(Exception e){
			return "twitchJsonError";
		}
    }

    private String longToSteam(long communityid){	    
		long constMinus = 76561197960265728L;
		communityid = communityid - constMinus;
		long authserver = communityid % 2;
		communityid = communityid-authserver;
		long authid = communityid/2;
		return "0:"+authserver+":"+authid;
    }
    
    private String getDivFromETF2L(String steamId){
	
		//REQUEST
		URLConnection connection = null;
		String url = "http://etf2l.org/feed/player/?steamid=" + steamId;
		String result = "";
		String team = ""; 
		String type = "";
		String division = "";

		try{
			URL serverAddress = new URL(url);
			connection = serverAddress.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
									connection.getInputStream()));
				while(in.ready())
			result = result + in.readLine();
			in.close();
			result = result.trim().replaceFirst("^([\\W]+)<","<");
		}
		catch(Exception e){
			return "Error while opening connection to ETF2L";
		}
		finally{
			//close?
		}

		//XML-aids
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(result));
			Document doc = builder.parse(is);
			NodeList nodeList = doc.getElementsByTagName("team");
			
			for (int i = 0; i < nodeList.getLength(); i++){
				Node node = nodeList.item(i);
				if (node.hasAttributes()) {
					Attr attr = (Attr) node.getAttributes().getNamedItem("type");
					if (attr != null ) {
						if (attr.getTextContent().equals("6on6")){
							type = node.getAttributes().getNamedItem("type").getTextContent();
							team = node.getAttributes().getNamedItem("name").getTextContent();	
							NodeList children = node.getChildNodes();
							Attr divisionAttr = (Attr) children.item(children.getLength()-2).getAttributes().getNamedItem("division");
							division = divisionAttr.getTextContent();
							break;
						}
						if (attr.getTextContent().equals("Highlander")){
							type = node.getAttributes().getNamedItem("type").getTextContent();
							team = node.getAttributes().getNamedItem("name").getTextContent();	
							NodeList children = node.getChildNodes();
							Attr divisionAttr = (Attr) children.item(children.getLength()-2).getAttributes().getNamedItem("division");
							division = divisionAttr.getTextContent();
						}
					}
				}
			}
			if (team.equals(""))
				return "No current ETF2L team";
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return "Error while reading ETF2L XML";
			}
		return (division + " " + team + " (" + type + ")");
    }
	
	private Boolean errorHandling(String targetSteam, String channel, String target){
		
		if ( targetSteam.equals("noSteamId") ){
				sendMessage(channel, target + " has not linked his Steam account to Twitch. Use !connect for more info");
				return true;
		}
		else if ( targetSteam.equals("twitchConnectionError") ){
			sendMessage(channel, target + ": there has been an error contacting the Twitch servers");
			return true;
		}
		else if ( targetSteam.equals("twitchJsonError") ){
			sendMessage(channel, target + " Error while reading JSON from Twitch");
			return true;
		}
		else{
			return false;
		}
		
	}
}