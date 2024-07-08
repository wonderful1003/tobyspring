package tobyspring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
	
	private ConnectionMaker connectionMaker;

	public void setConnectionMaker(ConnectionMaker connectionMaker) {
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
//	public static void main(String[] args) throws ClassNotFoundException, SQLException{
//		
//		UserDao dao = new UserDao();
//		
//		User user = new User();
//		user.setId("wonderful");
//		user.setName("방승현");
//		user.setPassword("married");
//		
//		dao.add(user);
//		
//		System.out.println(user.getId() + " success");
//		
//		User user2 = dao.get(user.getId());
//		System.out.println(user2.getName());
//		System.out.println(user2.getPassword());
//		System.out.println(user2.getId()+"조회성공");
//		
//	}	
}
