import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Comparator;
import com.github.koraktor.steamcondenser.steam.servers.*;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import java.net.*;

 
 public class FragCheck{												
 
	private static String IPInfo;
 
	public FragCheck(String Info){
		IPInfo = Info;
	}
	
	public String getMessage(){
		try{
			String msg = "Couldn't check frags";
				try{
					String[] parts = IPInfo.split(":");
					InetAddress serverIp = InetAddress.getByName(parts[0]);
					SourceServer server = new SourceServer(serverIp, Integer.parseInt(parts[1]));
					server.initialize();
					System.out.println("server");
					HashMap<String, SteamPlayer> players = server.getPlayers();
					System.out.println(players);
					ScoreComparator comp = new ScoreComparator(players);
					TreeMap<String, SteamPlayer> sorted_players = new TreeMap<String, SteamPlayer>(comp);
					sorted_players.putAll(players);
					System.out.println(sorted_players);
					msg = "";
					for (SteamPlayer value : sorted_players.values()) {
						if ( value.getScore() >= 0 )
							msg = msg + value.getName() + ": " + value.getScore() + "  ||  ";
						if ( value.getScore() < 0 )
							msg = msg + value.getName() + ": invalid score  ||  ";
					}
				}
				catch(Exception e){
					return e.getMessage();
				}
			return msg;
		}
		catch(Exception e){
			return "An error occured contacting the gameserver";
		}
	}
	
}

class ScoreComparator implements Comparator<String> {
    Map<String, SteamPlayer> base;
    public ScoreComparator(Map<String, SteamPlayer> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (base.get(a).getScore() >= base.get(b).getScore()){
            return -1;
        } else{
            return 1;
        }
    }
}