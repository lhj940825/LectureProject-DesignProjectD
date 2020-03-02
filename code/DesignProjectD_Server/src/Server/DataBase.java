package Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class DataBase {
	private Connection con;
	private Statement stmt;
	private ResultSet rs;
	private PreparedStatement pstmt;

	private String url = "jdbc:mysql://163.180.116.125:3306/designd?useUnicode=true&characterEncoding=euckr";
	private String id = "designd";
	private String password = "1234";

	public DataBase()throws SQLException {

		String driverName = "org.gjt.mm.mysql.Driver"; 
		try {
			Class.forName(driverName);
			getConnection(); 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	public void getConnection() throws SQLException {
		con = DriverManager.getConnection(url, id, password);
	}

	public void createTable(String tableName) throws SQLException {
		stmt.executeUpdate(
				"CREATE TABLE "+tableName+" (idx int unsigned not null, name varchar(100), address varchar(100))");
	}
	
	public boolean confirmLogin(String user_id, String user_pwd) throws SQLException{
		pstmt = con.prepareStatement(new String("select * from user where id = '"+ user_id+ "' AND password =  '" +user_pwd + "' "));
		rs = pstmt.executeQuery();
		
		while(rs.next())
			return true;
		
		return false;
	}
	public JSONObject getRecentUserData(String user_id) throws SQLException{
		JSONObject json = new JSONObject();
		pstmt = con.prepareStatement(new String("select * from bodyweightscale where user_id = '"+ user_id+ "'  ORDER BY time DESC LIMIT 7"));
		rs = pstmt.executeQuery();
		
		JSONArray recentWeight = new JSONArray();
		
		while(rs.next()){
			JSONObject temp = new JSONObject();
			temp.put("date", rs.getTimestamp("time").toString());
			temp.put("weight", rs.getFloat("weight"));
			temp.put("bone", rs.getFloat("bone"));
			temp.put("fat", rs.getFloat("fat"));
			temp.put("muscle", rs.getFloat("muscle"));
			recentWeight.add(temp);
		}
		
		json.put("RecentWeightData4Graph", recentWeight);
		
		pstmt = con.prepareStatement(new String("select * from bloodglucosemeter where user_id = '"+ user_id+ "' ORDER BY time DESC LIMIT 7"));
		rs = pstmt.executeQuery();
		
		JSONArray recentBloodGlucose = new JSONArray();
		while(rs.next()){
			JSONObject temp = new JSONObject();
			temp.put("date", rs.getTimestamp("time").toString());
			temp.put("bloodglucose", rs.getInt("bloodglucose"));
			recentBloodGlucose.add(temp);
			System.out.println(temp);
		}
		
		json.put("RecentBloodGlucoseData4Graph", recentBloodGlucose);
		
		pstmt = con.prepareStatement(new String("select * from watercle where user_id = '"+ user_id+ "' ORDER BY time DESC LIMIT 1"));
		rs = pstmt.executeQuery();
		
		while(rs.next()){
			json.put("mass_of_water", rs.getInt("water"));
		}
		return json;
		
	}
	
	public JSONArray getConsultant(String user_id) throws SQLException {
		JSONArray Consultants = new JSONArray();
		pstmt = con.prepareStatement(new String("select name, job_name, id from expert where client_id LIKE '%" + user_id + "%';"));
		rs = pstmt.executeQuery();
		System.out.println("ID : " + user_id);
		System.out.println("Consult : ");
		while(rs.next()) {
			JSONObject temp = new JSONObject();
			temp.put("name", rs.getString("name"));
			temp.put("jobName", rs.getString("job_name"));
			temp.put("gender", rs.getString("id"));
			Consultants.add(temp);
			System.out.println(temp);
		}
		
		
		return Consultants;
	}
	
	public JSONArray getNonConsultant(String user_id) throws SQLException {
		JSONArray NonConsultants = new JSONArray();
		pstmt = con.prepareStatement(new String("select name, job_name, id from expert where client_id NOT LIKE '%" + user_id + "%';"));
		rs = pstmt.executeQuery();
		System.out.println("ID : " + user_id);
		System.out.println("Non_consult : " + user_id);
		while(rs.next()) {                   
			JSONObject temp = new JSONObject();
			temp.put("name", rs.getString("name"));
			temp.put("jobName", rs.getString("job_name"));
			temp.put("gender", rs.getString("id"));
			NonConsultants.add(temp);
			System.out.println(temp);
		}
		return NonConsultants;
	}
	
	public boolean AskConsult(String Client, String Expert) throws SQLException{
		pstmt = con.prepareStatement(new String("select client_id from expert where id = " + Expert + ";"));
		rs = pstmt.executeQuery();
		System.out.println("Client : " + Client);
		System.out.println("Expert : " + Expert);
		String temp = new String();
		if(rs.next())
			 temp = rs.getString("client_id");
		if(temp == "")
			pstmt = con.prepareStatement(new String("update expert set client_id = '" + Client + "' where id = '" + Expert + "';"));
		else
			pstmt = con.prepareStatement(new String("update expert set client_id = '" + temp + ","+ Client +"' where id = '" + Expert + "';"));
		
		pstmt.executeUpdate();
		return true;
	}
	
	public JSONArray getBoardData() throws SQLException{
		
		pstmt = con.prepareStatement(new String("select * from board ORDER BY time DESC LIMIT 20"));
		rs = pstmt.executeQuery();
		
		JSONArray boardContentArray = new JSONArray();
		while(rs.next()){
			JSONObject temp = new JSONObject();
			temp.put("title", rs.getString("title"));
			temp.put("contents", rs.getString("contents"));
			boardContentArray.add(temp);
		}
		
		return boardContentArray;
	}
	
	public void putData2Board()throws SQLException{
		pstmt = con.prepareStatement(new String("INSERT INTO "));
		rs = pstmt.executeQuery();
		
		
	}
	/*
	 * 
	 
	public void putMarkerData(ArrayList<MarkerContainer> markerlist) throws SQLException {
	    pstmt = con.prepareStatement(new String("DELETE FROM marker"));
        pstmt.executeUpdate();
		pstmt = con.prepareStatement("INSERT INTO marker values(?, ?, ?, ? ,?)");
		for (int i = 0; i < markerlist.size(); i++) {
			pstmt.setString(1, markerlist.get(i).getLatitude().toString());
			pstmt.setString(2, markerlist.get(i).getlongitude().toString());
			pstmt.setString(3, markerlist.get(i).getLocationName());
			pstmt.setString(4, markerlist.get(i).getLocationSummary());
			pstmt.setString(5, markerlist.get(i).getLocationType());
			
			pstmt.executeUpdate();
		}
		pstmt.close();
	}
	
	//clinet�κ��� ���۹��� latitude, longitude�� ���� �ð��� �Բ� DB�� ����
	public void putLocation(String latLng) throws SQLException{
		String []location = latLng.split(" ");
		pstmt = con.prepareStatement("INSERT INTO latLng value(?, ?, ?)");
		pstmt.setString(1,location[0]);//location[0] == latitude
		pstmt.setString(2,location[1]);//location[1] == longitude
		pstmt.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
		pstmt.executeUpdate();
		pstmt.close();
	}
	public void putDrinkingData(Integer massOfWater) throws SQLException{
		pstmt = con.prepareStatement("INSERT INTO massofwater value(?, ?)");
		pstmt.setInt(1, massOfWater);
		pstmt.setTimestamp(2,new Timestamp(System.currentTimeMillis()));
		pstmt.executeUpdate();
		pstmt.close();
	}
	*/
	public void closeConnection() throws SQLException {
		con.close();
	}
}
