package user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.runner.JUnitCore;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import user.domain.User;

public class UserDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}
	
	
	public void setJdbcContext(JdbcContext jdbcContext) {
	}	

	public void add(final User user) throws ClassNotFoundException, SQLException{
		this.jdbcTemplate.update("insert into user(id, name, password) values(?,?,?)",
				user.getId(),user.getName(),user.getPassword());
	}

	public void deleteAll() throws SQLException, ClassNotFoundException{
		this.jdbcTemplate.update("delete from user");
	}
	
	public User get(String id) throws ClassNotFoundException, SQLException{

		Connection c = dataSource.getConnection();
		
		PreparedStatement ps = c.prepareStatement("select id,name,password from user where id =?");
		
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		
		User user = null;
		if(rs.next()){
			user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("Password"));
		}
		
		rs.close();
		ps.close();
		c.close();
		
		if (user == null) throw new EmptyResultDataAccessException(1);
		
		return user;
	}

	public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException{
		Connection c = null;
		PreparedStatement ps = null;
		
		try {
			c = dataSource.getConnection();
			ps = stmt.makePreparedStatement(c); 
			ps.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}finally {
			if (ps != null){ try {ps.close(); } catch (SQLException e) {}}
			if (c != null) { try {c.close(); } catch (SQLException e) {}}
		}
	}
	
	public int getCount() throws SQLException{
		return this.jdbcTemplate.queryForObject("select count(*) from user", Integer.class);
	}
	
//	public static void main(String[] args) throws ClassNotFoundException, SQLException{
//		
//		JUnitCore.main("tobyspring.UserDaoTest");
//		
//	}	
}
