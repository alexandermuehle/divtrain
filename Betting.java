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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Betting{

	public Betting(){
		
	}
	
	public String createTable(){
		Connection c = null;
		Statement stmt = null;
        Statement stmt2 = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			stmt = c.createStatement();
			String drop = "DROP TABLE USERS";        
			stmt.executeUpdate(drop);
			stmt.close();

			stmt2 = c.createStatement();
			String sql = "CREATE TABLE USERS " +
				   "(ID TEXT PRIMARY KEY     NOT NULL," +
				   " CURRENT 		 INT	 NOT NULL," +
				   " BET 			 INT	 NOT NULL," +
				   " MONEY           REAL    NOT NULL)";
				   
			stmt2.executeUpdate(sql);
			stmt2.close();
			c.close();
		} catch ( Exception e ) {
		  return e.getMessage();
		}
		return "Table created successfully";
	}
	
	public String betting(String amount, String result, String sender){
		Connection c = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Statement stmt3 = null;
		int current = 0;
		int bet = 0;
		if ( amount.equals("all") ){
			bet = -1;
		}
		else{
			bet = Integer.parseInt(amount);
		}
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			stmt2 = c.createStatement();
			stmt3 = c.createStatement();
			
			switch(result) {
				case "win":
					current = 1;
					break;
				case "lose":
					current = -1;
					break;
			}
			ResultSet rs = stmt2.executeQuery( "SELECT * FROM USERS WHERE ID = '" + sender + "';" );
			if ( rs.next() ){
				if ( bet == -1 ) bet = rs.getInt("MONEY") + rs.getInt("BET");
				if ( rs.getInt("CURRENT") != 0 ){
					String sqlReset = "UPDATE USERS set MONEY = " + Integer.toString(rs.getInt("MONEY") + rs.getInt("BET")) + ", BET = 0, CURRENT = 0 WHERE ID = '" + sender + "';";
					stmt3.executeUpdate(sqlReset);
					stmt3.close();
					c.commit();
				}
				if ( rs.getInt("MONEY") + rs.getInt("BET") < bet ){
					String tmp = sender + ": You tried to bet more points (" + bet +") than you currently have (" + Integer.toString(rs.getInt("MONEY")) + ")";
					stmt2.close();
					c.close();
					return tmp;
				}
			}
			else{
				if ( bet == -1 ) bet = 2000;
				if ( bet > 2000 ){
					String tmp = sender + ": You tried to bet more points (" + bet + ") than you currently have (2000)";                
					stmt2.close();
					c.close();
					return tmp;
				}
			}
			String sql = "INSERT OR REPLACE INTO USERS (ID, CURRENT, BET, MONEY) " +
						 "VALUES ('" + sender + "', " 
						 +  current + ", "  
						 + 	bet + ", "
						 +	"COALESCE((SELECT MONEY FROM USERS WHERE ID = '" + sender + "') - " + bet + ", 2000 - " + bet + ")"
						 + 	");";
			stmt.executeUpdate(sql);
			stmt.close();
			stmt2.close();
			stmt3.close();
			c.commit();
			c.close();
			return sql;
		} catch(Exception e){
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			return e.getMessage();
		}
	}

	public String result(String result){
		int right = 0;
		int wrong = 0;

		if (result.equalsIgnoreCase("win")){
			right = 1;
			wrong = -1;
		}
		if (result.equalsIgnoreCase("lose")){
			right = -1;
			wrong = 1;
		}
		if (result.equalsIgnoreCase("draw")){
			right = 2;
		}
		Connection c = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Statement stmt3 = null;
		Statement stmt4 = null;
		Statement stmt5 = null;
		Statement stmt6 = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			stmt2 = c.createStatement();
			stmt3 = c.createStatement();
			stmt4 = c.createStatement();
			stmt5 = c.createStatement();
			stmt6 = c.createStatement();				
			//draw
			if (right == 2){
				ResultSet rt = stmt5.executeQuery( "SELECT * FROM USERS WHERE CURRENT != 0" );
				while ( rt.next() ) {
					int newMoney = rt.getInt("MONEY") + rt.getInt("BET");
					String sqlD = "UPDATE USERS set MONEY = " + Integer.toString(newMoney) + ", BET = 0, CURRENT = 0 WHERE ID = '" + rt.getString("ID") + "';";
					stmt6.executeUpdate(sqlD);
					c.commit();
				}
				rt.close();
			}
			//winning
			ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS WHERE CURRENT = " + right + ";" );
			while ( rs.next() ) {
				int newMoney = rs.getInt("MONEY") + rs.getInt("BET") + rs.getInt("BET");
				String sqlW = "UPDATE USERS set MONEY = " + Integer.toString(newMoney) + ", BET = 0, CURRENT = 0 WHERE ID = '" + rs.getString("ID") + "';";
				stmt2.executeUpdate(sqlW);
				c.commit();
				System.out.println( rs.getString("ID") + " won" );
			}
			//losing
			ResultSet rr = stmt3.executeQuery( "SELECT * FROM USERS WHERE CURRENT = " + wrong + ";" );
			while ( rr.next() ) {
				int newMoney = rr.getInt("MONEY");
				if ( rr.getInt("MONEY") == 0 ){
					newMoney = 69;
				}
				String sqlL = "UPDATE USERS set MONEY = " + Integer.toString(newMoney) + ", BET = 0, CURRENT = 0 WHERE ID = '" + rr.getString("ID") + "';";
				stmt4.executeUpdate(sqlL);
				c.commit();
			}
			rs.close();
			rr.close();
			stmt.close();
			stmt2.close();
			stmt3.close();
			stmt4.close();
			stmt5.close();
			stmt6.close();
			c.close();
			return "Points have been updated";
		}
		catch(Exception e){
			return e.getMessage();
		}
	}
	
	public String getLeaderboard(){
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS ORDER BY MONEY DESC;" );
			int i = 1;
			String tmp = "";
			while ( rs.next() ) {
				tmp = tmp + i + ": " + rs.getString("ID") + ": " + rs.getInt("MONEY") + " || ";
				i++;
				if ( i > 3 ) break;
			}
			rs.close();
			stmt.close();
			c.close();
			return tmp;
		}catch(Exception e){
			return e.getMessage();
		}
	}
	
	public String manualPoints(String user, String sign, String amount){
		Connection c = null;
		Statement stmt = null;
		try{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sql = "UPDATE USERS set MONEY = MONEY " + sign + " " + amount + " WHERE ID = '" + user + "';";
			stmt.executeUpdate(sql);
			c.commit();
			stmt.close();
			c.close();
			return "success";
		}
		catch(Exception e){
			return e.getMessage();
		}
	}
	
	public String getPoints(String sender){
		Connection c = null;
		Statement stmt = null;
		Statement stmt2 = null;
		String tmp = "error";
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:users.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			stmt2 = c.createStatement();
			String sql = "INSERT OR IGNORE INTO USERS (ID, CURRENT, BET, MONEY) " +
							 "VALUES ('" + sender
						 + "', 0"
						 +  ", 0"
						 +  ", 2000"
						 +  ");";
			stmt2.executeUpdate(sql);
			c.commit();
			String s = "";
			ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS WHERE ID = '" + sender + "';" );
			while ( rs.next() ) {
				s = "";
				switch(rs.getInt("CURRENT")) {
					case 0:
						s = "draw";
						break;
					case 1:
						s = "win";
						break;
					case -1:
						s = "loss";
						break;
				}
				tmp = sender + " " + rs.getInt("MONEY") + " points (" + Integer.toString(rs.getInt("BET")) + " open in betting - " + s + ")";
			}
			rs.close();
			stmt.close();
			stmt2.close();
			c.commit();
			c.close();
			return tmp;
		}catch(Exception e){
			return e.getMessage();
		}
	}
}
