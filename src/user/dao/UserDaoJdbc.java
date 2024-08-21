package user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import user.domain.Level;
import user.domain.User;

public class UserDaoJdbc implements UserDao{
	
	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private RowMapper<User> userMapper =  
		new RowMapper<User>() {
			public User mapRow(ResultSet rs, int rowNum) throws SQLException{ 
				User user = new User();
				user.setId(rs.getString("id"));
				user.setName(rs.getString("name"));
				user.setPassword(rs.getString("password"));
				user.setLevel(Level.valueOf(rs.getInt("level")));
				user.setLogin(rs.getInt("login"));
				user.setRecommend(rs.getInt("recommend"));
				return user; 
			}
		};
	
	public void add(final User user) throws ClassNotFoundException, SQLException{
		this.jdbcTemplate.update("insert into user(id, name, password, level, login, recommend) "
				+ "values(?,?,?,?,?,?)", user.getId(),user.getName(),user.getPassword(),
				user.getLevel().intValue(), user.getLogin(), user.getRecommend());
	}

	public void deleteAll() throws SQLException, ClassNotFoundException{
		this.jdbcTemplate.update("delete from user");
	}
	
	public User get(String id) throws ClassNotFoundException, SQLException{
		return this.jdbcTemplate.queryForObject("select * from user where id =?",
			new Object[] {id}, this.userMapper);
	}

	public int getCount() throws SQLException{
		return this.jdbcTemplate.queryForObject("select count(*) from user", Integer.class);
	}
	
	public List<User> getAll() {
		return this.jdbcTemplate.query("select * from user order by id", this.userMapper);
	}
	
//	public static void main(String[] args) throws ClassNotFoundException, SQLException{
//		
//		JUnitCore.main("tobyspring.UserDaoTest");
//		
//	}	
}
