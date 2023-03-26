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
			"(3)--What equipment are unavailable?", "(4)--What are the hours of operation of MAC facility?",
			"(5)--What time is drop-in basketball?",
			"(6)--Can I book off the MAC arena for a student union event on March 15, 2023",
			"(7)--Can I add an equipment in the RAC workout area?",
			"(8)--What percentage of events are booked by internal guests for this year?",
			"(9)----What time are events scheduled in any of the facilities starting from March 12, 2023 until now?",
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

			// special cases
			if (input == 6) {
				System.out.println(
						"--If the query returns 0, then the MAC arena is available for booking on March 15, 2023. Otherwise, it is already booked.");
			} else if (input == 7) {
				System.out.println(
						"--If the query returns 0, then no equipment is available for addition. Otherwise, an equipment can be added to the RAC workout area.");
			}

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
						+ "FROM event\r\n" + "INNER JOIN event_schedule ON event.eventID = event_schedule.eventID\r\n"
						+ "INNER JOIN room ON event_schedule.roomID = room.roomID\r\n"
						+ "WHERE room.description = 'Studio 2'\r\n"
						+ "AND datetime_start BETWEEN '2023-03-01 00:00:00' AND '2023-03-31 23:59:59';";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 3:
				query = "SELECT equipment.description, equipment.state\r\n" + "FROM equipment\r\n"
						+ "WHERE equipment.state = 'Unavailable'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 4:
				query = "SELECT facility.name, facility.hours\r\n" + "FROM facility\r\n"
						+ "WHERE facility.name = 'MAC'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 5:
				query = "SELECT dropin_schedule.datetime_start, dropin_schedule.activity_name, room.description\r\n"
						+ "FROM dropin_schedule\r\n"
						+ "INNER JOIN dropin_activities ON dropin_schedule.activity_name = dropin_activities.activity_name\r\n"
						+ "INNER JOIN room ON dropin_schedule.roomID = room.roomID\r\n"
						+ "WHERE dropin_activities.activity_name = 'Basketball'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 6:
				query = "SELECT COUNT(*) AS num_events_scheduled FROM event_schedule\r\n"
						+ "WHERE datetime_start <= '2023-03-15 23:59:59'\r\n"
						+ "AND datetime_end >= '2023-03-15 00:00:00'\r\n"
						+ "AND roomID IN (SELECT roomID FROM facility_room WHERE facilityID = 2)";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 7:
				query = "SELECT COUNT(*) AS num_available_equipment FROM equipment WHERE in_room=3 AND state='Available'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 8:
				query = "SELECT \r\n"
						+ "    COUNT(*) * 100.0 / (SELECT COUNT(*) FROM event_schedule WHERE strftime('%Y', datetime_start) = strftime('%Y', 'now')) AS percentage\r\n"
						+ "FROM \r\n" + "    event_schedule es\r\n" + "    JOIN event e ON es.eventID = e.eventID\r\n"
						+ "    JOIN guest g ON e.booked_by = g.guestID\r\n" + "WHERE \r\n"
						+ "    g.type IN ('INTERNAL')\r\n"
						+ "    AND strftime('%Y', datetime_start) = strftime('%Y', 'now')";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 9:
				query = "SELECT \r\n"
						+ "    es.datetime_start, f.name AS facility_name, r.location AS room_location\r\n"
						+ "FROM \r\n" + "    event_schedule es\r\n"
						+ "    JOIN facility_room fr ON es.roomID = fr.roomID\r\n"
						+ "    JOIN facility f ON fr.facilityID = f.facilityID\r\n"
						+ "    JOIN room r ON es.roomID = r.roomID\r\n" + "WHERE \r\n"
						+ "    es.datetime_start > '2023-03-12 00:00:00' and\r\n"
						+ "    es.datetime_start <= datetime('now')";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 10:
				query = "SELECT \r\n" + "    e.description AS equipment_description, r.location AS room_location\r\n"
						+ "FROM \r\n" + "    equipment e\r\n" + "    JOIN room r ON e.in_room = r.roomID\r\n"
						+ "WHERE \r\n" + "    e.state = 'Stolen'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
			case 11:
				query = "SELECT \r\n"
						+ "    e.equipmentID, e.description AS equipment_description, r.location AS room_location, e.state\r\n"
						+ "FROM \r\n" + "    equipment e\r\n" + "    JOIN room r ON e.in_room = r.roomID\r\n"
						+ "    WHERE e.state = 'Available'";
				TMURecApp.printQuery(query, stmt, input);
				stmt.close();
				break;
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
			try {
				input = scanner.nextInt();
			} catch (Exception e) {
				System.out.println("Invalid input. Please try again.");
				scanner.nextLine();
				continue;
			}
			TMURecApp.executeStatement(input, scanner, app);
		}
	}
}
