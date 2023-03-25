package main.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

public class TMURecApp {
	private static String[] choiceArray = new String[] { "(1)--Equipment available in each facility?",
			"(2)--What events are upcoming for the RAC Studio 2 room in the month of march?",
			"(3)--What equipment are unavailable?", "(4)--What are the hours of operation of x facility?",
			"(5)--What time is drop-in x?",
			"(6)--Can I book off the MAC arena for a student union event on March 15, 2023?",
			"(7)--Can I add an equipment in the RAC workout area?",
			"(8)--What percentage of events are made from internal guests for this year?",
			"(9)--What time are events scheduled in any of the facilities?",
			"(10)--What equipment is stolen and in what room?",
			"(11)--List all the available equipments and what room they are in" };

	Connection connection;

	private Connection openConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection("jdbc:sqlite:resource/test.db");
			System.out.println("Opened database successfully");
		} catch (Exception e) {
			System.err.println(e);
		}

		return connection;
	}

	private static void printQuery(String query, Statement stmt, int input) {
		System.out.println("-----------Fetching Query-----------");
		try {
			System.out.println(TMURecApp.choiceArray[input - 1]);
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsMeta = rs.getMetaData();
			int numColumns = rsMeta.getColumnCount();
			for (int i = 1; i <= numColumns; i++) {
				System.out.printf("|%-20s", rsMeta.getColumnName(i));
			}
			System.out.println("");
			while (rs.next()) {
				for (int i = 1; i <= numColumns; i++) {
					Object value = rs.getObject(i);
					System.out.printf("%-22s", value);
				}
				System.out.println();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		System.out.println("----------------------\n");
	}

	private static void executeStatement(int input, Scanner scanner, TMURecApp app) {

		try {
			Statement stmt = app.connection.createStatement();
			String query = null;
			switch (input) {
			case -1:
				System.out.println("System exiting...");
				System.exit(0);
			case 1:		
				query = "SELECT facility.name AS facility_name, equipment.description\r\n" + "FROM facility\r\n"
						+ "INNER JOIN facility_room ON facility.facilityID = facility_room.facilityID\r\n"
						+ "INNER JOIN room ON facility_room.roomID = room.roomID\r\n"
						+ "INNER JOIN equipment ON room.roomID = equipment.in_room;";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 2:
				query = "SELECT event.description, event_schedule.datetime_start, event_schedule.datetime_end\r\n"
						+ "FROM event\r\n"
						+ "INNER JOIN event_schedule ON event.eventID = event_schedule.eventID\r\n"
						+ "INNER JOIN room ON event_schedule.roomID = room.roomID\r\n"
						+ "WHERE room.description = 'Studio 2'\r\n"
						+ "AND datetime_start BETWEEN '2023-03-01 00:00:00' AND '2023-03-31 23:59:59';";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 3:
				query = "SELECT equipment.description, equipment.state\r\n"
						+ "FROM equipment\r\n"
						+ "WHERE equipment.state = 'Unavailable'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 4:
				query = "SELECT facility.name, facility.hours\r\n"
						+ "FROM facility\r\n"
						+ "WHERE facility.name = 'MAC'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
			default:
				System.out.println("Invalid input. Please try again.");
				return;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	public static void main(String args[]) {
		// intialize class object
		TMURecApp app = new TMURecApp();

		// get connection
		app.connection = app.openConnection();

		// Execute statements
		System.out.println("System starting.....\nHello! Please input 1-10 below to query the database");
		System.out.println("To exit, please input -1");

		// user input loop
		Scanner scanner = new Scanner(System.in);
		int input = 0;
		while (input != -1) {
			for (String prompt : app.choiceArray) {
				System.out.println(prompt);
			}
			System.out.print(">>");
			input = scanner.nextInt();
			TMURecApp.executeStatement(input, scanner, app);
		}
	}
}
