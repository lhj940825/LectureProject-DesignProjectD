package Server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerThread extends Thread {
	private static List<ServerThread> threads = new ArrayList<ServerThread>();

	Socket clientSocket = null;
	DataBase db;

	public ServerThread(Socket socket) throws SQLException {
		// TODO Auto-generated constructor stub
		super();
		this.clientSocket = socket;
		this.db = new DataBase();
		threads.add(this);
		for (int i = 0; i < threads.size() - 1; i++) {
			if (socket.getInetAddress().equals(threads.get(i).clientSocket.getInetAddress())) {
				System.out.println(threads.get(i).clientSocket.getInetAddress() + "Duplicated Access");
				System.out.println(threads.get(i).getName() + "Delete Thread");
				threads.remove(i);
			} else {
				System.out.println(threads.get(i).clientSocket.getInetAddress() + "Access");
			}
		}
		for (int i = 0; i < threads.size(); i++) {
			System.out.println(threads.get(i).getName() + "Activated Thread");
		}

	}

	@SuppressWarnings("unchecked")
	public void run() {

		InputStream objectInBytes = null;
		ObjectInputStream ois = null;
		OutputStream sendingBytes = null;
		ObjectOutputStream oos = null;

		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			objectInBytes = clientSocket.getInputStream();
			ois = new ObjectInputStream(objectInBytes);
			sendingBytes = clientSocket.getOutputStream();
			oos = new ObjectOutputStream(sendingBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try {
			while (true) {
				Object obj = ois.readObject();

				if (obj instanceof String) {
					processJSONData((String) obj, oos);
					/*
					 * String request = (String) obj; if
					 * (request.equals("login")) {
					 * 
					 * 
					 * oos.writeObject("login");
					 * 
					 * } else { oos.writeObject("receive"); }
					 */

				} else if (obj instanceof Integer) { // this code is about

				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JSONObject getRecentWeatherInfo(String[] data) throws SQLException {
		JSONObject json = null;
		return json;
	}

	public JSONObject getRecentAllWeatherInfo() throws SQLException {
		JSONObject json = null;

		return json;
	}

	public JSONObject getWeatherPlanetInfo(String[] data) throws IOException, SQLException {
		JSONObject json = null;
		return json;
	}

	private void sendMessageAll(String message) {
		// TODO Auto-generated method stub
		ServerThread thread;
		for (int i = 0; i < threads.size(); i++) {
			thread = threads.get(i);
			if (thread.isAlive())
				thread.sendMessage(this, message);
		}
	}

	public void sendMessage(ServerThread talker, String message) {
		/*
		 * try { oos.writeObject(message); oos.reset(); } catch (IOException
		 * ioe) { }
		 */
	}

	public void sendNotification(String message) throws IOException {
		OutputStream sendingBytes2 = null;
		ObjectOutputStream oos2 = null;
		sendingBytes2 = clientSocket.getOutputStream();
		oos2 = new ObjectOutputStream(sendingBytes2);
		oos2.writeObject(message);
		oos2.reset();
	}

	public void processJSONData(String jsonString, ObjectOutputStream oos) throws ParseException, IOException, SQLException {

		JSONObject inputJson = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		inputJson = (JSONObject) jsonParser.parse(jsonString);

		if (inputJson.get("request").equals("Login")) {
			System.out.println("ID=" + inputJson.get("id"));
			System.out.println("PWD=" + inputJson.get("pwd"));

			JSONObject outputJson = new JSONObject();
			if (db.confirmLogin((String) inputJson.get("id"), (String) inputJson.get("pwd"))) {

				outputJson.put("response", "LoginConfirm");
				outputJson.put("id", inputJson.get("id"));
				outputJson.put("board", getBoardData());
				outputJson.put("health", getHealthData((String) inputJson.get("id")));
				outputJson.put("ConsultList", db.getConsultant(inputJson.get("id").toString()));
				outputJson.put("NonConsultList", db.getNonConsultant(inputJson.get("id").toString()));
				System.out.println(outputJson.toString());
			} else {
				outputJson.put("response", "LoginDeny");
			}
			oos.writeObject(outputJson.toString());

		} else if (inputJson.get("request").equals("UpdatePost")) {
			System.out.println("Title = " + inputJson.get("title"));
			System.out.println("Content = " + inputJson.get("content"));
			System.out.println("ID = " + inputJson.get("id"));

			// TODO update input post data to DB
			

		} else if (inputJson.get("request").equals("SignUp")) {
			System.out.println(inputJson);
			// TODO update input User Sign up data to DB

			JSONObject outputJson = new JSONObject();
			outputJson.put("response", "SignUpConfirm");
			oos.writeObject(outputJson.toString());
		} else if (inputJson.get("request").equals("EditInfo")) {
			System.out.println(inputJson);

			// TODO User info to DB
			JSONObject outputJson = new JSONObject();
			outputJson.put("response", "EditInfoConfirm");
			oos.writeObject(outputJson.toString());

		} else if (inputJson.get("request").equals("UpdateGlucose")) {
			// TODO get Recent Users data(Age, Height, Weight, sex ) And Call
			// Function diagnosisDiabetes() with blood glucose and those datas
			// TODO Update glucose Info to DB

			System.out.println(inputJson);

			JSONObject outputJson = new JSONObject();
			outputJson.put("response", "DiagnosisDiabetes");
			if (diagnosisDiabetes(1.0d, 27, Double.parseDouble(inputJson.get("bloodGlucose").toString()), 24.0d))
				outputJson.put("result", "positive");
			else
				outputJson.put("result", "negative");

			oos.writeObject(outputJson.toString());

		} else if (inputJson.get("request").equals("UpdateScale")) {
			// TODO Update Scale Info to DB
			System.out.println(inputJson);
		} else if (inputJson.get("request").equals("UpdateWater")) {
			// TODO Update Water Info to DB
			System.out.println(inputJson);
		} else if (inputJson.get("request").equals("SendMessage")) {
			
		} else if (inputJson.get("request").equals("Ask")) {
			System.out.println(inputJson);
			JSONObject outputJson = new JSONObject();
			outputJson.put("response", "AskResult");
			if (db.AskConsult(inputJson.get("client").toString(), inputJson.get("expert").toString()))
				outputJson.put("result", "OK");
			else
				outputJson.put("result", "NO");
		} 
	}

	public JSONObject getHealthData(String user_id) throws SQLException {

		// TODO Get Recent Health data For Drawing Health Graph
		JSONObject recentUserData = db.getRecentUserData(user_id);

		return recentUserData;
	}

	public JSONArray getBoardData() throws SQLException {

		return (JSONArray) db.getBoardData();
	}

	public boolean diagnosisDiabetes(double sex, int age, double blood_glucose, double BMI) {
		// Initialize Weights and bias
		double[] weight = new double[4];
		weight[0] = -1.0730865f;
		weight[1] = 0.10259177f;
		weight[2] = 0.03559787f;
		weight[3] = -0.23719087f;
		double bias = 1.43384278f;

		double idxOfAge = Age2Idx(age);

		double hypothesis = sex * weight[0] + idxOfAge * weight[1] + blood_glucose * weight[2] + BMI * weight[3] + bias;
		double probability = sigmoid(hypothesis);

		if (probability > 0.5f)
			return true;
		else
			return false;
	}

	public double sigmoid(double x) {
		return (1 / (1 + Math.pow(Math.E, (-1 * x))));
	}

	public double Age2Idx(int age) {
		if (age < 20)
			return 0.0d;
		else if (age >= 20 && age <= 24)
			return 1.0d;
		else if (age >= 25 && age <= 26)
			return 2.0d;
		else if (age >= 27 && age <= 28)
			return 3.0d;
		else if (age >= 29 && age <= 30)
			return 4.0d;
		else if (age >= 31 && age <= 32)
			return 5.0d;
		else if (age >= 33 && age <= 34)
			return 6.0d;
		else if (age >= 35 && age <= 36)
			return 7.0d;
		else if (age >= 37 && age <= 38)
			return 8.0d;
		else if (age >= 39 && age <= 40)
			return 9.0d;
		else if (age >= 41 && age <= 42)
			return 10.0d;
		else if (age >= 43 && age <= 44)
			return 11.0d;
		else if (age >= 45 && age <= 46)
			return 12.0d;
		else if (age >= 47 && age <= 48)
			return 13.0d;
		else if (age >= 49 && age <= 50)
			return 14.0d;
		else if (age >= 51 && age <= 52)
			return 15.0d;
		else if (age >= 53 && age <= 54)
			return 16.0d;
		else if (age >= 55 && age <= 56)
			return 17.0d;
		else if (age >= 57 && age <= 58)
			return 18.0d;
		else if (age >= 59 && age <= 60)
			return 19.0d;
		else if (age >= 61 && age <= 62)
			return 20.0d;
		else if (age >= 63 && age <= 64)
			return 21.0d;
		else if (age >= 65 && age <= 66)
			return 22.0d;
		else if (age >= 67 && age <= 68)
			return 23.0d;
		else if (age >= 69 && age <= 70)
			return 24.0d;
		else if (age >= 71 && age <= 72)
			return 25.0d;
		else if (age >= 73 && age <= 74)
			return 26.0d;
		else if (age >= 75)
			return 27.0d;
		return 0.0d;

	}

}
