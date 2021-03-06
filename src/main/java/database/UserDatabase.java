package database;

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
//import org.json.JSONArray;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class UserDatabase {

	public static void main(String[] args) throws Exception {
//		createUserTable();
		//insertIntoUserTable("Test", "x@x.com", "aofengen", "xxxxxxxx");
//		checkUserTable("a", "b");
//		dropUserTable();
	}
	
//	private static void dropUserTable() throws Exception {
//		Connection c = null;
//		Statement stmt = null;
//		try {
//			Class.forName("org.postgresql.Driver");
//			c = getConnection();
//			System.out.println("Opened database successfully");
//			
//			stmt = c.createStatement();
//			String sql = "DROP TABLE USERS";
//			stmt.executeQuery(sql);
//			stmt.close();
//			c.close();
//		} catch (Exception e) {
//			System.err.println( e.getClass().getName() + ": " + e.getMessage());
//		}
//		System.out.println("Table dropped successfully");
//	}
	
	public static void createUserTable() throws Exception {
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
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Users table created successfully");
		c.close();
	}
	
	

	public static JSONObject signup(String name, String email, String username, String password) throws Exception {
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

			String encryptedPassword = passEncrypt(password);
			
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = ( SELECT MAX (id) FROM users );");
			
			if (rs.next()) {
				id = rs.getInt("id") + 1;
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
			
			obj.put("id", id);
			obj.put("name", name);
			obj.put("email", email);
			obj.put("username", username);
			obj.put("Record Created", tC);
			obj.put("Record Updated", tU);
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
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
		
		c.commit();
		c.close();
		
		return obj;
	}

	private static String passEncrypt(String password) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String encryptedPassword = passwordEncryptor.encryptPassword(password);
		return encryptedPassword;
	}

	public static JSONObject login(String email, String password) throws Exception {
		Connection c = null;
		Statement stmt = null;

		JSONObject message = new JSONObject();
		try {

			Class.forName("org.postgresql.Driver");
			c = getConnection();
			System.out.println("Opened database successfully");
			c.setAutoCommit(false);
			
			stmt = c.createStatement();
			ResultSet rs = findEmailInDB(email, stmt);
			boolean passMatch = passwordCheck(rs, password);
			
			if (passMatch) {
				message = getUserInfo(rs);
			} else {
				message.put("error", "Invalid Login Attempt");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		stmt.close();
		c.close();
		return message;
	}
	
	public static JSONObject changeInfo(String newName, String newEmail, String newUsername, String password, String token, int id) throws Exception {
		Connection c = null;
		Statement stmt = null;
		JSONObject obj = new JSONObject();
		
		Date timeU = new Date();
		java.sql.Timestamp tU = new Timestamp(timeU.getTime());
		
		boolean tokenMatch = checkToken(token);
		if (tokenMatch) {
			try {
				Class.forName("org.postgresql.Driver");
				c = getConnection();
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");
				
				stmt = c.createStatement();

				ResultSet rs = findUserInDB(id, stmt);

				boolean passMatch = passwordCheck(rs, password);
				
				stmt.close();
				if (passMatch) {
					PreparedStatement ps = c.prepareStatement("UPDATE USERS SET NAME = ?, USERNAME = ?, EMAIL = ?, TIMEUPDATED = ? WHERE ID = ?");
					
					ps.setString(1, newName);
					ps.setString(2, newUsername);
					ps.setString(3, newEmail);
					ps.setTimestamp(4, tU);
					ps.setInt(5, id);
					ps.executeUpdate();
					ps.close();
				} else {
					obj.put("error", "Invalid Password!");
				}
			} catch (Exception e) {
				System.out.println(e);
			}
			
			obj.put("id", id);
			obj.put("New Name", newName);
			obj.put("New Email", newEmail);
			obj.put("New Username", newUsername);
			obj.put("Record Updated", tU);
		} else {
			obj.put("error", "Invalid or missing token!");
		}
		c.commit();
		c.close();
		
		return obj;
	}
	
	public static JSONObject changePassword(String oldPass, String newPass, String token, int id) throws Exception {
		Connection c = null;
		Statement stmt = null;
		JSONObject obj = new JSONObject();
		
		Date timeU = new Date();
		java.sql.Timestamp tU = new Timestamp(timeU.getTime());
		
		boolean tokenMatch = checkToken(token);
		if (tokenMatch) {
			try {
				Class.forName("org.postgresql.Driver");
				c = getConnection();
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");
				
				stmt = c.createStatement();

				ResultSet rs = findUserInDB(id, stmt);

				boolean passMatch = passwordCheck(rs, oldPass);
								
				stmt.close();
				if (passMatch) {
					String encPassword = passEncrypt(newPass);
					PreparedStatement ps = c.prepareStatement("UPDATE USERS SET PASSWORD = ?, TIMEUPDATED = ? WHERE ID = ?");
					
					ps.setString(1, encPassword);
					ps.setTimestamp(2, tU);
					ps.setInt(3, id);
					ps.executeUpdate();
					ps.close();
					
					obj.put("id", id);
					obj.put("Password Update", "Successful!");
					obj.put("Record Updated", tU);
				} else {
					obj.put("error", "Invalid Password!");
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			obj.put("error", "Invalid or missing token!");
		}
		c.commit();
		c.close();
		
		return obj;
	}
	
	
	private static boolean passwordCheck(ResultSet rs, String password) throws SQLException {
		if (rs.next()) {
			String encPassword = rs.getString("password");
			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

			if (passwordEncryptor.checkPassword(password, encPassword)) {
			
				System.out.println("Password Match!");
				return true;
			} else {
				System.out.println("No Match!");
				return false;
			}
		} else {
			System.out.println("No Result Set Found!");
			return false;
		}
	}

	private static Connection getConnection() throws URISyntaxException, SQLException {
	    String dbUrl = System.getenv("JDBC_DATABASE_URL");
		return DriverManager.getConnection(dbUrl);
//	    return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/blackjack", "postgres",
//			"9074dewberry1136");
	}
	
	private static ResultSet findEmailInDB(String email, Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE email = '" + email + "';");
		return rs;
	}
	
	private static ResultSet findUserInDB(int id, Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE id = " + id + ";");
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
			System.err.println(e);
		}
		
		return obj;
	}
	
	private static boolean checkToken(String token) throws Exception {
		boolean tokenValid;
		try {
		    Algorithm algorithm = Algorithm.HMAC256("i_am_secret");
		    JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
		    DecodedJWT jwt = verifier.verify(token);
		    tokenValid = true;
		} catch (Exception e){
		    System.err.println(e);
		    tokenValid = false;
		}
		return tokenValid;
	}


}
