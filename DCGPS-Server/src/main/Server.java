package main;

import java.io.IOException;
import java.sql.*;

public class Server {

	// learn.bcit.ca - 199.30.177.52
	// www.bcit.ca - 142.232.77.1

	// JDBC driver name and database URL

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/gpstracker";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "comp4985cmst";

	public static void main(String[] args) {
		Thread t;
		try {
			t = new ServerThread();
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean writeToDB() {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			String sql = "INSERT INTO `clientlogin` (`username`, `firstname`, `lastname`, `password`) VALUES ('dlee','Delaney','Lee', 'password')";
			stmt.executeUpdate(sql);
			System.out.println("Inserted records into the table...");

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
			return false;
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
			return false;
			
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null) {
					conn.close();
					return true;
				}
			} catch (SQLException se) {
				return false;
			}
			try {
				if (conn != null) {
					conn.close();
					return true;
				}
			} catch (SQLException se) {
				se.printStackTrace();
				return false;
			}
		}
		return true;
	}

}
