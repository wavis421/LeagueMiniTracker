package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class MiniTrackerDatabase {
	// SSH/Database connect constants
	private static final int SSH_PORT = 22;
	private static final String SSH_HOST = "ec2-52-53-129-63.us-west-1.compute.amazonaws.com";
	private static final String SSH_USER = "ec2-user";
	private static final String SSH_KEY_FILE_PATH = "./mini-tracker-key.pem";
	private static final String DATABASE = "MiniTracker";
	private static final String DB_USER = "LeagueStudent";

	// Student model database indices
	private static final int CLIENT_ID_IDX = 0;
	private static final int FIRST_NAME_IDX = 1;
	private static final int LAST_INITIAL_IDX = 2;
	private static final int CURRENT_CLASS_IDX = 3;
	private static final int CURRENT_LEVEL_IDX = 4;
	private static final int HOME_LOC_CODE_IDX = 5;
	private static final int HOME_LOCATION_IDX = 6;
	private static final int GITHUB_NAME_IDX = 7;
	private static final int NUM_DB_COLUMNS = 8;
	private static final int NUM_DB_COL_NO_GITHUB = (NUM_DB_COLUMNS - 1);

	// MySql select commands: the concat adds commas between fields
	private static final String SELECT_STRING_WITH_GITHUB = "concat(ClientID, ',', FirstName, ',', LastInitial, ',', "
			+ "CurrentClass, ',', CurrentLevel, ',', HomeLocCode, ',', HomeLocation, ',', GithubName)";
	private static final String SELECT_STRING_NO_GITHUB = "concat(ClientID, ',', FirstName, ',', LastInitial, ',', "
			+ "CurrentClass, ',', CurrentLevel, ',', HomeLocCode, ',', HomeLocation)";

	// Save SSH Session
	private Session session = null;

	public MiniTrackerDatabase() {
		// Connect to Server via SSH Tunnel
		connectToServer();
	}

	public Session connectToServer() {
		// Check if need to re-connect
		if (session != null && !session.isConnected()) {
			session = null;
		}

		// Create new SSH connection
		if (session == null)
			return connectSSH();

		// Still connected from previous attempt
		return session;
	}

	private Session connectSSH() {
		try {
			java.util.Properties config = new java.util.Properties();
			JSch jsch = new JSch();
			session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
			jsch.addIdentity(SSH_KEY_FILE_PATH);
			config.put("StrictHostKeyChecking", "no");
			config.put("ConnectionAttempts", "2");
			session.setConfig(config);

			session.setServerAliveInterval(60 * 1000); // in milliseconds
			session.setServerAliveCountMax(20);
			session.setConfig("TCPKeepAlive", "yes");

			session.connect();
			return session;

		} catch (Exception e) {
			// Failed maximum connection attempts: disconnect session
			closeConnection();
			System.out.println("Failed to connect to SSH Tunnel: " + e.getMessage());
		}
		return null;
	}

	public void closeConnection() {
		if (session != null) {
			session.disconnect();
			session = null;
		}
	}

	public boolean isConnected() {
		if (session == null || !session.isConnected())
			return false;
		else
			return true;
	}

	public ArrayList<StudentMiniModel> getAllStudents() {
		// Get students with and without github
		// Note: BufferedReader does not allow NULL github fields, so get separately
		ArrayList<StudentMiniModel> list = getStudents(NUM_DB_COLUMNS, SELECT_STRING_WITH_GITHUB,
				"WHERE GithubName != ''");
		list.addAll(getStudents(NUM_DB_COL_NO_GITHUB, SELECT_STRING_NO_GITHUB, "WHERE GithubName IS NULL"));
		return list;
	}

	public ArrayList<StudentMiniModel> getStudentsByLevel(String level) {
		// Get students by Level, both with and without github
		// Note: BufferedReader does not allow NULL fields, set get separately
		String where = "WHERE CurrentClass != '' AND LEFT(CurrentClass,3) = '" + level + "@'";
		ArrayList<StudentMiniModel> list = getStudents(NUM_DB_COLUMNS, SELECT_STRING_WITH_GITHUB,
				where + " AND GithubName != ''");
		list.addAll(getStudents(NUM_DB_COL_NO_GITHUB, SELECT_STRING_NO_GITHUB, where + " AND GithubName IS NULL"));
		return list;
	}

	private ArrayList<StudentMiniModel> getStudents(int numColumns, String selectString, String where) {
		ArrayList<StudentMiniModel> list = new ArrayList<StudentMiniModel>();

		try {
			ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
			execChannel.setCommand("mysql -u " + DB_USER + " -e \"SELECT " + selectString + " FROM Students " + where
					+ "\" " + DATABASE);

			InputStream input = execChannel.getInputStream();
			execChannel.connect();

			waitForCommandExecution(execChannel);

			InputStreamReader inputReader = new InputStreamReader(input, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String line = null;
			boolean skipped = false;

			// Process each line in input stream
			while ((line = bufferedReader.readLine()) != null) {
				if (!skipped) {
					// First line is title, so skip
					skipped = true;
					continue;
				}

				// Data is comma separated string; split into array
				String[] lineArray = line.split("\\s*,\\s*", -1);
				if (lineArray.length == NUM_DB_COLUMNS) {
					// Add student to array list (with github)
					list.add(new StudentMiniModel(Integer.parseInt(lineArray[CLIENT_ID_IDX]), lineArray[FIRST_NAME_IDX],
							lineArray[LAST_INITIAL_IDX], lineArray[GITHUB_NAME_IDX], lineArray[CURRENT_CLASS_IDX],
							lineArray[CURRENT_LEVEL_IDX], lineArray[HOME_LOC_CODE_IDX], lineArray[HOME_LOCATION_IDX]));

				} else if (lineArray.length == NUM_DB_COL_NO_GITHUB) {
					// Add student to array list, no github field
					list.add(new StudentMiniModel(Integer.parseInt(lineArray[CLIENT_ID_IDX]), lineArray[FIRST_NAME_IDX],
							lineArray[LAST_INITIAL_IDX], "", lineArray[CURRENT_CLASS_IDX], lineArray[CURRENT_LEVEL_IDX], 
							lineArray[HOME_LOC_CODE_IDX], lineArray[HOME_LOCATION_IDX]));

				} else {
					System.out.println("Bad input (# columns " + lineArray.length + "): '" + line + "'");
					continue;
				}
			}
			System.out.println("Mini Tracker DB: get students " + list.size());
			bufferedReader.close();
			inputReader.close();
			input.close();

			execChannel.disconnect();
			return list;

		} catch (JSchException | IOException e) {
			System.out.println("MySql command failed: " + e.getMessage());
		}
		return null;
	}

	private void waitForCommandExecution(ChannelExec execChannel) {
		int sleepCount = 0;
		// Sleep for up to 10 seconds while the command executes
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception while waiting for command to finish: " + e.getMessage());
			}
		} while (!execChannel.isClosed() && sleepCount++ < 100);

		if (sleepCount >= 100)
			System.out.println("SELECT command timed out");
	}
}
