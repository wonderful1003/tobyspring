package tobyspring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.runner.JUnitCore;

public class UserDao {
	
	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void add(User user) throws ClassNotFoundException, SQLException{

		Connection c = dataSource.getConnection();
		
		PreparedStatement ps = c.prepareStatement("insert into user(id, name, password) values(?,?,?)");
		
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		c.close();
	}

	public User get(String id) throws ClassNotFoundException, SQLException{

		Connection c = dataSource.getConnection();
		
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
	
//	public abstract Connection getConnection() throws ClassNotFoundException, SQLException;
//	
//	public class NUserDao extends UserDao{
//		public Connection getConnection() throws ClassNotFoundException, SQLException{
//			Class.forName("com.mysql.jdbc.Driver");
//			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
//					+ "&serverTimezone=UTC","root","qwer!2345");
//			
//			return c;
//		}
//	}
//	
//	public class DUserDao extends UserDao{
//		public Connection getConnection() throws ClassNotFoundException, SQLException{
//			Class.forName("com.mysql.jdbc.Driver");
//			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
//					+ "&serverTimezone=UTC","root","qwer!2345");
//			
//			return c;
//		}
//	}
//	
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		
		JUnitCore.main("tobyspring.UserDaoTest");
		
	}	
}
