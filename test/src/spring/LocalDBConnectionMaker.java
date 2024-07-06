package spring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LocalDBConnectionMaker implements ConnectionMaker{

	@Override
	public Connection makeConnection() throws ClassNotFoundException, SQLException {
		Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
				+ "&serverTimezone=UTC","root","qwer!2345");
		
		return c;
	}

	
}
