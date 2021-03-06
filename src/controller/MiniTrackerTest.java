package controller;

import java.util.ArrayList;

import model.MiniTrackerDatabase;
import model.StudentMiniModel;

/**
 * Mini Tracker Test:
 * 
 *      This is sample code for testing the Student Mini Trackr Database.
 *      Final code would export the 'model' package to access the data.
 * 
 * @author wavis
 *
 */
public class MiniTrackerTest {
	public static void main(String[] args) {
		// Test all API's for Mini Tracker database
		MiniTrackerDatabase conn = new MiniTrackerDatabase();
		System.out.println("Check is connected: " + conn.isConnected());

		System.out.println("\nAll Students: ");
		printStudents(conn.getAllStudents());
		
		System.out.println("\nStudents Level 'AD': ");
		printStudents(conn.getStudentsByLevel("AD"));
		
		conn.closeConnection();
	}
	
	private static void printStudents(ArrayList<StudentMiniModel> list) {
		// Test by printing all students in list
		for (StudentMiniModel s : list) 
			System.out.println(s.toString());
		System.out.println("# Students: " + list.size());
	}
}
