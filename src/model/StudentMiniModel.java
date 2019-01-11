package model;

public class StudentMiniModel implements Comparable<StudentMiniModel> {
	int clientID;
	String firstName, lastInitial, githubName, currentClass, currentLevel;
	String locCode, location;

	public StudentMiniModel(int clientID, String firstName, String lastInitial, String githubName, String currentClass,
			String currentLevel, String locCode, String location) {
		this.clientID = clientID;
		this.firstName = firstName;
		this.lastInitial = lastInitial;
		this.githubName = githubName;
		this.currentClass = currentClass;
		this.currentLevel = currentLevel;
		this.locCode = locCode;
		this.location = location;
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

	public String getCurrentLevel() {
		return currentLevel;
	}

	public String getLocCode() {
		return locCode;
	}

	public String getLocation() {
		return location;
	}

	public String toString() {
		// For debug, print all fields of student object
		return "ID: " + clientID + ", " + firstName + " " + lastInitial + ", git: " + githubName + ", loc: " + location
				+ " (" + locCode + "), class: " + currentClass + "(" + currentLevel + ")";
	}

	@Override
	public int compareTo(StudentMiniModel other) {
		// Order student list by ascending Client ID
		if (clientID < other.getClientID())
			return -1;
		else if (clientID > other.getClientID())
			return 1;
		else
			return 0;
	}
}
