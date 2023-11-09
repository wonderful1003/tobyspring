package test2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
	
//	public Connection getConnection() throws ClassNotFoundException, SQLException{
//
//		Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
//				+ "&serverTimezone=UTC","root","qwer!2345");
//		
//		return c;
//	}
	
	private ConnectionMaker connectionMaker;
	
	public UserDao(ConnectionMaker connectionMaker) {		
		this.connectionMaker = connectionMaker;
	}

	public void add(User user) throws ClassNotFoundException, SQLException{
		
		Connection c = connectionMaker.makeConnection();
		
		PreparedStatement ps = c.prepareStatement("insert into user(id, name, password) values(?,?,?)");
		
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		c.close();
	}

	public User get(String id) throws ClassNotFoundException, SQLException{
		
		Connection c = connectionMaker.makeConnection();
		
		PreparedStatement ps = c.prepareStatement("select id,name,password from user where id =?");
		
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("Password"));
		
		rs.close();
		ps.close();
		c.close();
		
		return user;
	}
	
}
