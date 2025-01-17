package project.ids;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.catalina.ha.backend.Sender;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {
	private String jdbcUrl = "jdbc:mysql://localhost:3306/IDS_DB?serverTimezone=Asia/Seoul";
	private String dbId = "ks";
	private String dbPass = "ks";
	private DataSource dataSource;
	private static final String jdbcDriver = "jdbc:apache:commons:dbcp:/pool";
	private Connection connection = null;
	private static DatabaseConnection instance = null;
	public static DatabaseConnection getInstance() throws SQLException, NamingException, ClassNotFoundException {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}
	
	/* 테스트용 커넥션 */
	public DatabaseConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/* 운용 커넥션(커넥션풀) 
	private DatabaseConnection() throws NamingException, SQLException, ClassNotFoundException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Class.forName("com.mysql.cj.jdbc.Driver");
		//Context context = new InitialContext();
		//dataSource = (DataSource) context.lookup("java:comp/env/jdbc/IDS_DB");
	} */
	
	//private Connection getConnection() throws SQLException {
	//	return dataSource.getConnection();
	//
	//}
	
	// 등록된 디바이스 개수 조회 -> deviceID 정하는데 사용
	public byte countDeviceID() throws SQLException {
		byte count = -1;
		
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		
		String query = "SELECT COUNT(*) FROM devices";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet resultSet = pstmt.executeQuery();
		
		if(resultSet.next()) {
			count = resultSet.getByte("count(*)");
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return count;
	}

	// 등록된 디바이스 조회
	// 여러 개의 검색결과가 있을 수 있으므로 ArrayList로 반환
	public ArrayList<DeviceTableDTO> selectRegisteredDevice() throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<DeviceTableDTO> deviceList = new ArrayList<>();
		
		// 조인된 결과는 별도의 테이블이 필요
		String query = 
				"SELECT devices.sensor_id, devices.group_id, devices.device_id, devices.location, status.action, status.time " +
				"FROM devices, status " +
				"WHERE devices.sensor_id = status.sensor_id " +
				"AND devices.group_id = status.group_id " +
				"AND devices.device_id = status.device_id " +
				"AND status.id in (SELECT max(status.id) FROM status GROUP BY status.device_id)";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			DeviceTableDTO deviceTableDTO = new DeviceTableDTO();
			deviceTableDTO.setSensorID(resultSet.getByte("devices.sensor_id"));
			deviceTableDTO.setGroupID(resultSet.getByte("devices.group_id"));
			deviceTableDTO.setDeviceID(resultSet.getByte("devices.device_id"));
			deviceTableDTO.setLocation(resultSet.getString("devices.location"));
			deviceTableDTO.setAction(resultSet.getString("status.action"));
			deviceTableDTO.setMeasurementTime(resultSet.getTimestamp("time"));
			
			deviceList.add(deviceTableDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	
		return deviceList;
	}
	
	// 전체 로그 조회
	public ArrayList<LogTableDTO> selectLogList() throws SQLException {
		//Connection connection = getConnection();
		//connection = DriverManager.getConnection(jdbcDriver);
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<LogTableDTO> logList = new ArrayList<>();
		
		String query = 
				"SELECT status.time, devices.location, status.action, status.sensor_data " +
				"FROM devices, status " +
				"WHERE devices.sensor_id = status.sensor_id " +
				"AND devices.group_id = status.group_id " +
				"AND devices.device_id = status.device_id " +
				"ORDER BY status.time asc";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			LogTableDTO logTableDTO = new LogTableDTO();
			logTableDTO.setMeasurementTime(resultSet.getTimestamp("status.time"));
			logTableDTO.setLocation(resultSet.getString("devices.location"));
			logTableDTO.setAction(resultSet.getString("status.action"));
			logTableDTO.setSensorData(resultSet.getInt("status.sensor_data"));
			
			logList.add(logTableDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return logList;
	}
	
	// 개별 로그 조회
	public ArrayList<LogTableDTO> selectDeviceLogList(int sensorID, int groupID, int deviceID) throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<LogTableDTO> deviceLogList = new ArrayList<>();
		
		String query = 
				"SELECT status.time, devices.location, status.action, status.sensor_data " +
				"FROM devices, status " +
				"WHERE (devices.sensor_id = ? AND status.sensor_id = ?) " +
				"AND (devices.group_id = ? AND status.group_id = ?) " +
				"AND (devices.device_id = ? AND status.device_id = ?) " +
				"ORDER BY status.time asc";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setInt(1, sensorID);
		pstmt.setInt(2, sensorID);
		pstmt.setInt(3, groupID);
		pstmt.setInt(4, groupID);
		pstmt.setInt(5, deviceID);
		pstmt.setInt(6, deviceID);
		ResultSet resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			LogTableDTO logTableDTO = new LogTableDTO();
			logTableDTO.setMeasurementTime(resultSet.getTimestamp("time"));
			logTableDTO.setLocation(resultSet.getString("location"));
			logTableDTO.setAction(resultSet.getString("action"));
			logTableDTO.setSensorData(resultSet.getInt("sensor_data"));
			deviceLogList.add(logTableDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return deviceLogList;
	}
	
	// locations 테이블 조회
	public ArrayList<LocationDTO> selectLocationsTable() throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<LocationDTO> locationList = new ArrayList<>();
		
		String query = "SELECT * FROM locations ";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			LocationDTO locationDTO = new LocationDTO();
			locationDTO.setGroupID(resultSet.getByte("group_id"));
			locationDTO.setLocation(resultSet.getString("location"));
			locationList.add(locationDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return locationList;
	}
	
	// 디바이스 제거 
	public void deleteDevice(byte sensorID, byte groupID, short deviceID) throws SQLException { // 지우고자하는 디바이스ID
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String deviceDeleteQuery = "DELETE FROM devices WHERE sensor_id=? " +
									"AND group_id=? " + "AND device_id=? ";
		
		PreparedStatement pstmt = connection.prepareStatement(deviceDeleteQuery);
		pstmt.setByte(1, sensorID);
		pstmt.setByte(2, groupID);
		pstmt.setShort(3, deviceID);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 상태정보 제거
	public void deleteStatus(byte sensorID, byte groupID, short deviceID) throws SQLException { // 지우고자하는 디바이스ID
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String statusDeleteQuery = "DELETE FROM status WHERE sensor_id=? " +
									"AND group_id=? " + "AND device_id=? ";
		
		PreparedStatement pstmt = connection.prepareStatement(statusDeleteQuery);
		pstmt.setByte(1, sensorID);
		pstmt.setByte(2, groupID);
		pstmt.setShort(3, deviceID);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 디바이스 추가
	public void insertDevice(byte sensorID, byte groupID, short deviceID, String mac, String location) throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String query = "INSERT INTO devices VALUES(?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setByte(1, sensorID);
		pstmt.setByte(2, groupID);
		pstmt.setShort(3, deviceID);
		pstmt.setString(4, mac);
		pstmt.setString(5, location);
		pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 로그 저장
	public void insertLogTable(byte sensorID, byte groupID, short deviceID, String action, int sensorData) throws SQLException {
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String query = "INSERT INTO status(sensor_id, group_id, device_id, action, sensor_data, time) VALUES(?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setByte(1, sensorID);
		pstmt.setByte(2, groupID);
		pstmt.setShort(3, deviceID);
		pstmt.setString(4, action);
		pstmt.setInt(5, sensorData);
		pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 등록요청된 디바이스 저장
	public void insertRequestedDevice(byte sensorID, String mac) throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String query = "INSERT INTO unregistered_devices VALUES(?, ?)";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, mac);
		pstmt.setInt(2, sensorID);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 등록요청된 디바이스 select
	public ArrayList<UnregisteredDevice> selectUnregisteredDevices() throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<UnregisteredDevice> unregisteredDeviceList = new ArrayList<>();
		
		String query = "SELECT * FROM unregistered_devices ";
		PreparedStatement pstmt = connection.prepareStatement(query);
		ResultSet resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			UnregisteredDevice unregisteredDeviceDTO = new UnregisteredDevice();
			unregisteredDeviceDTO.setSensorID(resultSet.getByte("sensor_id"));
			unregisteredDeviceDTO.setMac(resultSet.getString("mac_addr"));
			unregisteredDeviceList.add(unregisteredDeviceDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return unregisteredDeviceList;
	}
	
	// 등록된 디바이스 제거
	public void deleteRegisteredDevice(String mac) throws SQLException {
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String query = "DELETE FROM unregistered_devices WHERE mac_addr=?";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, mac);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// locations 테이블 삽입 메소드
	public void insertLocations(int groupID, String location) throws SQLException {
		//connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		String query = "INSERT INTO locations VALUES(?, ?)";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setInt(1, groupID);
		pstmt.setString(2, location);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 이미 존재하는 디바이스 확인
	public Device checkRegisterdDevice(String mac) throws SQLException {
		Device device = null;
		int count = 0;
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		
		String query = "SELECT * FROM devices WHERE mac_addr = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, mac);
		ResultSet resultSet = pstmt.executeQuery();
		
		if(resultSet.next()) {
			byte sensorID = resultSet.getByte("sensor_id");
			byte groupID = resultSet.getByte("group_id");
			byte deviceID = resultSet.getByte("device_id");
			String macAddress = resultSet.getString("mac_addr");
			device = new Device(sensorID, groupID, deviceID, macAddress);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		if(device != null) return device;
		return null;
	}
	
	// 등록된 디바이스 가져오기
	public Device getDevice(String mac) throws SQLException {
		Device device = null;
		connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		ArrayList<UnregisteredDevice> unregisteredDeviceList = new ArrayList<>();
		
		String query = "SELECT * FROM devices WHERE mac_addr = ?";
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, mac);
		ResultSet resultSet = pstmt.executeQuery();
		
		if(resultSet.next()) {
			byte sensorID = resultSet.getByte("sensor_id");
			byte groupID = resultSet.getByte("group_id");
			byte deviceID = resultSet.getByte("device_id");
			String mac_addr = resultSet.getString("mac_addr");
			device = new Device(sensorID, groupID, deviceID, mac_addr);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return device;
	}
	
	/* 디바이스 위치 업데이트
	public void updateDevicePosition(int deviceID, String position) throws SQLException { // 바꾸고자하는 디바이스ID, 위치값
		connection = DriverManager.getConnection(jdbcDriver);
		//Connection connection = getConnection();
		String query = "UPDATE device SET position=? WHERE id=?";
		
		PreparedStatement pstmt = connection.prepareStatement(query);
		pstmt.setString(1, position);
		pstmt.setInt(2, deviceID);
		pstmt.executeUpdate(); 
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	*/
}
