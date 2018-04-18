package blackjack.database;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.jasypt.util.password.StrongPasswordEncryptor;

import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class Database {

	public static void main(String[] args) throws Exception {
//		createUserTable();
		//insertIntoUserTable("Test", "x@x.com", "aofengen", "xxxxxxxx");
//		checkUserTable("a", "b");
	}
	
	public static void createUserTable() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = getConnection();
			System.out.println("Opened database successfully");
			
			stmt = c.createStatement();
			String sql = "CREATE TABLE USERS " +
					"(ID INT PRIMARY KEY 		NOT NULL," +
					"NAME		 	TEXT		NOT NULL," +
					"EMAIL		 	TEXT		NOT NULL," +
					"USERNAME    	TEXT    	NOT NULL," +
					"PASSWORD   	TEXT	    NOT NULL," + 
					"TIMECREATED    TIMESTAMP   NOT NULL," +
					"TIMEUPDATED    TIMESTAMP   NOT NULL)";
			stmt.executeQuery(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Table created successfully");
	}

	public static JSONObject signup(String name, String email, String username, String password) {
		Connection c = null;
		int id = 0;
		
		Date timeC = new Date();
		Date timeU = new Date();
		
		java.sql.Timestamp tC = new Timestamp(timeC.getTime());
		java.sql.Timestamp tU = new Timestamp(timeU.getTime());
		
		JSONObject obj = new JSONObject();
		try {
			Class.forName("org.postgresql.Driver");
			c = getConnection();
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
			String encryptedPassword = passwordEncryptor.encryptPassword(password);
			System.out.println(encryptedPassword);
			
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = ( SELECT MAX (id) FROM users );");
			
			if (rs.next()) {
				id = rs.getInt(1) + 1;
			} else {
				id = 1;
			}
			stmt.close();
			
			PreparedStatement pstmt = c.prepareStatement("INSERT INTO USERS (ID, NAME, EMAIL, USERNAME, PASSWORD, TIMECREATED, TIMEUPDATED)"
	            + "VALUES (?, ?, ?, ?, ?, ?, ?)");
			pstmt.setInt(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, email);
			pstmt.setString(4, username);
			pstmt.setString(5, encryptedPassword);
			pstmt.setTimestamp(6, tC);
			pstmt.setTimestamp(7, tU);
			
			pstmt.executeUpdate();
			pstmt.close();
			c.commit();
			c.close();
			
			obj.put("id", id);
			obj.put("name", name);
			obj.put("email", email);
			obj.put("username", username);
			obj.put("Record Created", tC);
			obj.put("Record Updated", tU);
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Record added successfully");
		
		String token = "";
		
		try {
			Algorithm alg = Algorithm.HMAC256("i_am_secret");
			token = JWT.create().withIssuer("auth0").sign(alg);
			
			obj.put("token", token);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return obj;
	}

	public static JSONObject login(String email, String password) {
		Connection c = null;
		Statement stmt = null;

		JSONObject message = new JSONObject();
		try {

			Class.forName("org.postgresql.Driver");
			c = getConnection();
			System.out.println("Opened database successfully");
			
			stmt = c.createStatement();
			ResultSet rs = findEmailInDB(email, stmt);
			boolean passMatch = checkPassword(rs, password);
			
			if (passMatch) {
					message = getUserInfo(rs);
			} else {
				message.put("error", "Invalid Login Attempt");
			}

			stmt.close();
			c.close();
	} catch (Exception e) {
		System.out.println(e);
	}
		return message;
}
	
	private static boolean checkPassword(ResultSet rs, String password) throws SQLException {
		if (rs.next()) {
			String encPassword = rs.getString("password");
			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

			if (passwordEncryptor.checkPassword(password, encPassword)) {
			
				System.out.println("Record Found! ");
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static Connection getConnection() throws URISyntaxException, SQLException {
//	    String dbUrl = System.getenv("JDBC_DATABASE_URL");
//		return DriverManager.getConnection(dbUrl);
	    return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/blackjack", "postgres",
			"9074dewberry1136");
	}
	
	private static ResultSet findEmailInDB(String email, Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE email = '" + email + "';");
		return rs;
	}

	private static JSONObject getUserInfo(ResultSet rs) throws Exception {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", rs.getInt("id"));
	    	obj.put("email", rs.getString("email"));
	    	obj.put("name", rs.getString("name"));
	    	obj.put("username", rs.getString("username"));
	    	
	    	Algorithm alg = Algorithm.HMAC256("i_am_secret");
			String token = JWT.create().withIssuer("auth0").sign(alg);
			
			obj.put("token", token);	
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return obj;
	}
	
}