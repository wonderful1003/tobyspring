package tobyspring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DConnectionMaker implements ConnectionMaker{

	@Override
	public Connection makeConnection() throws ClassNotFoundException, SQLException {
		// 양천향교
//		Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
//				+ "&serverTimezone=UTC","root","qwer!2345");
		// 명동		
		Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
				+ "&serverTimezone=UTC","root","admin");
		
		return c;
	}
}