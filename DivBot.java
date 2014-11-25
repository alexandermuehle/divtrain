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
import com.github.koraktor.steamcondenser.steam.servers.*;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DivBot extends PircBot {
    
		public static String LAST_FM;
		
		//regex for !lookup name
		public static Pattern prof = Pattern.compile("^(!profile)\\b");
		public static Pattern ip = Pattern.compile("^(!server)\\b");
		public static Pattern map = Pattern.compile("^(!map)\\b");
        public static Pattern frags = Pattern.compile("^(!frags)\\b");
        public static Pattern fragcheck = Pattern.compile("^(!fragcheck)\\b");
		public static Pattern song = Pattern.compile("^(!song)\\b");
		public static Pattern song2 = Pattern.compile("^(!np)\\b");
		public static Pattern hours = Pattern.compile("^(!hours)\\s*(\\w*)\\b");
		public static Pattern stats = Pattern.compile("^(!stats)\\s*(\\w*)\\b");
		public static Pattern log = Pattern.compile("^(!log)\\b");
		public static Pattern backpack = Pattern.compile("^(!bp)\\b");
		public static Pattern credit = Pattern.compile("^(!credit)\\b");
		public static Pattern conn = Pattern.compile("^(!connect)\\b");
		public static Pattern comm = Pattern.compile("^(!command)\\b");
		public static Pattern toggle = Pattern.compile("^(!toggle)\\s*(\\w*)\\s*\\z");
		public static Pattern betting = Pattern.compile("^(!bet)\\s*((?:\\d+)|all)\\s+(win|lose)\\s*\\z");
		public static Pattern oBetting = Pattern.compile("^(!openbetting)\\b");
		public static Pattern cBetting = Pattern.compile("^(!closebetting)\\b");
		public static Pattern rBetting = Pattern.compile("^(!result)\\s+(win|lose|draw)");
		public static Pattern points = Pattern.compile("^(!points)\\s*(\\w*)\\b");
        public static Pattern balance = Pattern.compile("^(!balance)\\b");
        public static Pattern mybalance = Pattern.compile("^(!mybalance)\\b");
		public static Pattern table = Pattern.compile("^(!createtable)\\b");
		public static Pattern leader = Pattern.compile("^(!leaderboard)\\b");
        public static Pattern manual = Pattern.compile("^(!manualpoints)\\s+(\\w+)\\s+(\\+|\\-)\\s+(\\d+)");
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
		public boolean backB = true;
		public boolean bettingB = false;
        public boolean pointsB = true;
		
		public SimpleDateFormat timeStamp;
		public Betting bet;
		
    public DivBot(String lastfm) {
        this.setName("SlinBot");
		LAST_FM = lastfm;
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
		bet = new Betting();
    }
    
	//react to private message
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		mods = message;
		System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": updated mods");
	}
	
	//on connect
	public void onUserList(String channel, User[] users) {
		sendMessage(channel, "/mods");
		System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": getting mods");
	}
	
    // react to messages
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
		String channelName = channel.substring(1);

        //fragcheck
        m = fragcheck.matcher(message);
        if (m.find()) {
			String targetSteam = getSteamFromTwitch(channelName, channel);
			String IPInfo = getServerFromSteam(targetSteam);
			FragCheck check = new FragCheck(IPInfo);
			System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": checking frags (" +sender+")");
			sendMessage(channel, check.getMessage() + ";");
			return;
        }		
		
		//CREATE TABLE
		m = table.matcher(message);
		if (m.find()){
			if ( channelName.equalsIgnoreCase(sender) || sender.equalsIgnoreCase("lexsoor") ){
				String msg = bet.createTable();
				sendMessage(channel, msg);
				System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
			}
			return;
		}
		
		//BETTING
        m = betting.matcher(message);
		if ( m.find() ){
			if ( !bettingB ) {
				sendMessage(channel, "Betting is not open at this time");
				System.out.println(channel + ": This command has been disabled (betting)"); //LOGGING
				return;
			}
			String msg = bet.betting(m.group(2), m.group(3), sender);
			if ( ! msg.contains("INSERT OR ") ) sendMessage(channel, msg);
			System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
			return;
		}
		
		//OPEN BETTING
		m = oBetting.matcher(message);
		if (m.find()) {
			if ( channelName.equalsIgnoreCase(sender) || sender.equalsIgnoreCase("lexsoor") ){
				bettingB = true;
				sendMessage(channel, "Betting is now open");
			}
            else{
                sendMessage(channel, "Only the owner of the channel can open/close bets");            
            }
		}
		
		//CLOSE BETTING
		m = cBetting.matcher(message);
		if (m.find()) {
			if ( channelName.equalsIgnoreCase(sender) || sender.equalsIgnoreCase("lexsoor") ){
				bettingB = false;
				sendMessage(channel, "Betting is now closed");
			}
            else{
                sendMessage(channel, "Only the owner of the channel can open/close bets");
            }
		}
		
		//RESULT BETTING
        m = rBetting.matcher(message);
		if (m.find()) {
			if (  channelName.equalsIgnoreCase(sender) || sender.equalsIgnoreCase("lexsoor") ){
				bettingB = false;
				pointsB = false;
				sendMessage(channel, "Points are being updated");
				String msg = bet.result(m.group(2));
				sendMessage(channel, msg);
				System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
				pointsB = true;
				return;
			}
			sendMessage(channel, "Only the channel owner can enter results");
			return;
		}
		
		//LEADERBOARD
		m = leader.matcher(message);
		if (m.find()) {
			String msg = bet.getLeaderboard();
			sendMessage(channel, msg);
			System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
			return;
		}

        //MANUAL POINTS
        m = manual.matcher(message);
        if (m.find()){
            if (  channelName.equalsIgnoreCase(sender) || sender.equalsIgnoreCase("lexsoor") ){
                if ( !pointsB ){
                    sendMessage(channel, "Points are being updated");
                    return;
                }
				String msg = bet.manualPoints(m.group(2), m.group(3), m.group(4));
				sendMessage(channel, msg);
				System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
				return;
            }
			sendMessage(channel, "Only the channel owner can give and take points manually");
        }
		
		//POINTS
		if ( ( points.matcher(message).find()) || (balance.matcher(message).find()) || (mybalance.matcher(message).find())) {
            if ( !pointsB ){
                sendMessage(channel, "Points are being updated");
                return;
            }
            String msg = bet.getPoints(sender);
			sendMessage(channel, msg);
			System.out.println(timeStamp.format(Calendar.getInstance().getTime()) + ": " + msg);
			return;
		}
		
		//PROFILE
		m = prof.matcher(message);
		if (m.find()) {
			if ( !profB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (profile)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = channelName;	//channel = name
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String msg = "Profile: http://steamcommunity.com/profiles/" + targetSteam;
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
				msg = "No Song playing on lastfm right now";
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
			String target = channelName;
			String game = m.group(2);	//group 2 = game
			if ( game.equals("") ){
				game = "tf2";
			}
			if ( !game.equalsIgnoreCase("tf2") && !game.equalsIgnoreCase("csgo") && !game.equalsIgnoreCase("dota2") ){
				sendMessage(channel, sender + ": please specify the game you want stats of as follows: Dota 2 = dota2, TeamFortress 2 = tf2, CS:GO = csgo");
				return;
			}
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target)){
				if (game.equalsIgnoreCase("tf2")){
					sendMessage(channel, "Stats: http://logs.tf/profile/" + targetSteam);
					System.out.println(channel + ": http://logs.tf/profile/" + targetSteam);
				}
			 	if (game.equalsIgnoreCase("dota2")){
					sendMessage(channel, "Stats: dotabuff.com/players/" + targetSteam);
					System.out.println(channel + ": dotabuff.com/players/" + targetSteam);
				}
				if (game.equalsIgnoreCase("csgo")){
					sendMessage(channel, "Stats: http://csgo-stats.com/" + targetSteam);
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
			String target = channelName;	//channel = name
			
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String logLink = getLogLink(targetSteam);
				String msg = ": Last game: " + logLink;
				sendMessage(channel, msg);
				System.out.println(channel + ": " + logLink); //LOGGING
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

			String target = channelName;	//channel = name
			//getting steam from twitch
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String IPInfo = getServerFromSteam(targetSteam);
				try{
					String[] parts = IPInfo.split(":");
					InetAddress serverIp = InetAddress.getByName(parts[0]);
					SourceServer server = new SourceServer(serverIp, Integer.parseInt(parts[1]));
					server.initialize();
					System.out.println(server.getServerInfo());
						if( Boolean.TRUE.equals(server.getServerInfo().get("passwordProtected"))){
							String msg = "Server info is private (passworded server)";
							sendMessage(channel, msg);
							return;
						}
				}
				catch(Exception e){
					e.printStackTrace(System.out);
					sendMessage(channel, e.getMessage());
					return;
				}
				String msg = "Server Info: connect " + IPInfo;
				System.out.println(channel + ": " + IPInfo); //LOGGING
				sendMessage(channel, msg);
			}
		}
		
		//MAP
		m = map.matcher(message);
		if (m.find()) {
			String target = channelName;	//channel = name
			String targetSteam = getSteamFromTwitch(target, channel);
			String IPInfo = getServerFromSteam(targetSteam);
			String msg = "map didnt work";
				try{
					String[] parts = IPInfo.split(":");
					InetAddress serverIp = InetAddress.getByName(parts[0]);
					SourceServer server = new SourceServer(serverIp, Integer.parseInt(parts[1]));
					server.initialize();
					msg = "Map: " + server.getServerInfo().get("mapName");
				}
				catch(Exception e){
					e.printStackTrace(System.out);
					sendMessage(channel, e.getMessage());
					return;
				}
				sendMessage(channel, msg);
		}
		
		//HOURS
		m = hours.matcher(message);
		if ( m.find() ) {
			if ( !hoursB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (hours)"); //LOGGING
				return;
			}
			String target = channelName;
			String game = m.group(2);

			if ( game.equals("") ){
				game = "tf2";
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
					sendMessage(channel, sender + ": There has been a Steam Connection Error"); 
				}
				else{
					msg = targetHours + msgGame;
					sendMessage(channel, msg);
					System.out.println(channel + ": " + targetHours + msgGame); //LOGGING
				}
			}
		}
		
		//BACKPACK
		m = backpack.matcher(message);
		if (m.find()) {
			if ( !backB ) {
				sendMessage(channel, "This command has been disabled");
				System.out.println(channel + ": This command has been disabled (backpack)"); //LOGGING
				return;
			}
			//getting steam from twitch
			String target = channelName;	//channel = name
			
			String targetSteam = getSteamFromTwitch(target, channel);
			if ( !errorHandling(targetSteam, channel, target) ){
				String backpackLink = getBackpackLink(targetSteam);
				String msg = "Backpack: " + backpackLink;
				sendMessage(channel, msg);
				System.out.println(channel + ": " + backpackLink); //LOGGING
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
			sendMessage(channel, "Commands: !server || !stats [game] || !hours [game] || !profile || !log || !np || !credit");
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
	
	private String getBackpackLink(String targetSteam){
		return "http://backpack.tf/u/" + targetSteam;
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
								return player.getString("gameserverip");
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
