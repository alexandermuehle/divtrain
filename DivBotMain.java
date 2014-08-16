import org.jibble.pircbot.*;

public class DivBotMain {
    
    public static void main(String[] args) throws Exception {
        
        // startup
        DivBot bot = new DivBot(args[1]);
        
        // Enable debugging output.
        bot.setVerbose(true);
        
        // Connect to the IRC server.
        bot.connect("irc.twitch.tv", 6667, "oauth:79q0m8yusto9vn5udlaxxvmf1ap2x17");

        // Join the #pircbot channel.
        bot.joinChannel(args[0]);
        
    }
    
}