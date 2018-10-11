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
	private static final int GITHUB_NAME_IDX = 3;
	private static final int LOCATION_IDX = 4;
	private static final int CURRENT_CLASS_IDX = 5;
	private static final int DUMMY_DATA_IDX = 6;
	private static final int NUM_DB_COLUMNS = 7;

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
		return getStudents("");
	}

	public ArrayList<StudentMiniModel> getActiveStudents() {
		return getStudents("WHERE DummyData = 0");
	}

	public ArrayList<StudentMiniModel> getDummyStudents() {
		return getStudents("WHERE DummyData = 1");
	}

	public ArrayList<StudentMiniModel> getStudentsByLevel(int level) {
		return getStudents("WHERE CurrentClass != '' AND LEFT(CurrentClass,2) = '" + String.valueOf(level) + "@'");
	}

	private ArrayList<StudentMiniModel> getStudents(String where) {
		ArrayList<StudentMiniModel> list = new ArrayList<StudentMiniModel>();

		try {
			ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
			execChannel.setCommand("mysql -u " + DB_USER
					+ " -e \"SELECT concat(ClientID, ',', FirstName, ',', LastInitial, ',', GithubName, ',', "
					+ "Location, ',', CurrentClass, ',', DummyData) FROM Students " + where + "\" " + DATABASE);
			InputStream input = execChannel.getInputStream();
			execChannel.connect();

			InputStreamReader inputReader = new InputStreamReader(input);
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String line = null;
			boolean skipped = false;

			// Process each line in input stream
			while ((line = bufferedReader.readLine()) != null) {
				// Until data is ready, reader returns line of NULL
				if (line.equals("NULL"))
					continue;

				if (!skipped) {
					// First line is title, so skip
					skipped = true;
					continue;
				}

				// Data is comma separated string; split into array
				String[] lineArray = line.split("\\s*,\\s*");
				if (lineArray.length < NUM_DB_COLUMNS) {
					System.out.println("Bad input (# columns " + lineArray.length + "): " + line);
					continue;
				}

				// Add student to array list
				list.add(new StudentMiniModel(Integer.parseInt(lineArray[CLIENT_ID_IDX]), lineArray[FIRST_NAME_IDX],
						lineArray[LAST_INITIAL_IDX], lineArray[GITHUB_NAME_IDX],
						Integer.parseInt(lineArray[LOCATION_IDX]), lineArray[CURRENT_CLASS_IDX],
						(lineArray[DUMMY_DATA_IDX].equals("1")) ? true : false));
			}
			bufferedReader.close();
			inputReader.close();

			execChannel.disconnect();
			return list;

		} catch (JSchException | IOException e) {
			System.out.println("MySql command failed: " + e.getMessage());
		}
		return null;
	}
}
