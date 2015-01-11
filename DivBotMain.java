import org.jibble.pircbot.*;

public class DivBotMain {
    
    public static void main(String[] args) throws Exception {
        
        // startup
        DivBot bot = new DivBot(args[1]);	
        
        // Enable debugging output.
        bot.setVerbose(false);
        
        // Connect to the IRC server.
        bot.connect("irc.twitch.tv", 6667, "oauth:3hqvqc36tssr6v7jtn9npk3sejj2l5r");

        // Join the #pircbot channel.
        bot.joinChannel(args[0]);
        
    }
    
}
