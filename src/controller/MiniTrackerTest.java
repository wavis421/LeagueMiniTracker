package controller;

import java.util.ArrayList;

import model.MiniTrackerDatabase;
import model.StudentMiniModel;

public class MiniTrackerTest {
	public static void main(String[] args) {
		// Test all API's for Mini Tracker database
		MiniTrackerDatabase conn = new MiniTrackerDatabase();
		System.out.println("Check is connected: " + conn.isConnected());

		System.out.println("\nAll Students: ");
		printStudents(conn.getAllStudents());
		
		System.out.println("\nDummy Students: ");
		printStudents(conn.getDummyStudents());
		
		System.out.println("\nActive Students: ");
		printStudents(conn.getActiveStudents());
		
		System.out.println("\nStudents Level 3: ");
		printStudents(conn.getStudentsByLevel(3));
		
		conn.closeConnection();
	}
	
	private static void printStudents(ArrayList<StudentMiniModel> list) {
		// Test by printing all students in list
		for (StudentMiniModel s : list) 
			System.out.println(s.toString());
	}
}
