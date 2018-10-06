package model;

public class StudentModel {
	int clientID;
	String firstName, lastInitial, githubName, currentClass;
	int location;
	boolean dummyData;

	public StudentModel(int clientID, String firstName, String lastInitial, String githubName, int location,
			String currentClass, boolean dummyData) {
		this.clientID = clientID;
		this.firstName = firstName;
		this.lastInitial = lastInitial;
		this.githubName = githubName;
		this.location = location;
		this.currentClass = currentClass;
		this.dummyData = dummyData;
	}

	public int getClientID() {
		return clientID;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastInitial() {
		return lastInitial;
	}

	public String getGithubName() {
		return githubName;
	}

	public String getCurrentClass() {
		return currentClass;
	}

	public int getLocation() {
		return location;
	}

	public boolean isDummyData() {
		return dummyData;
	}

	public String toString() {
		// For debug, print all fields of student object
		return "ID: " + clientID + ", " + firstName + " " + lastInitial + ", git: " + githubName + ", loc: " + location
				+ ", class: " + currentClass + ", dummy: " + dummyData;
	}
}
