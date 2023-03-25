package main.java;

import java.sql.*;

public class TMURecApp {
	
	
	private Connection openConnection() {
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:resource/test.db");
		} catch (Exception e) {
			System.err.println("Problem Encountered");
		}
		System.out.println("Opened database successfully");
		return c;
	}

	public static void main(String args[]) {
		//intialize class object
		TMURecApp app = new TMURecApp();
		
		//get connection
		Connection c = app.openConnection();
		
		
	}
}
