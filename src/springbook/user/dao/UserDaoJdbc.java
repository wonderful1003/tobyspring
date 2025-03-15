package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import springbook.user.domain.Level;
import springbook.user.domain.User;
import springbook.user.sqlservice.SqlService;

public class UserDaoJdbc implements UserDao{
	
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private Map<String, String> sqlMap;
	
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}
	
	@Autowired
	private SqlService sqlService;
	
//	public void setSqlService(SqlService sqlService) {
//		this.sqlService = sqlService;
//	}

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
				user.setEmail(rs.getString("email"));
				return user; 
			}
		};
	
	public void add(final User user) throws ClassNotFoundException, SQLException{
		this.jdbcTemplate.update(this.sqlService.getSql("userAdd"), user.getId(),user.getName(),user.getPassword(),
				user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getEmail());
	}

	public void deleteAll() throws SQLException, ClassNotFoundException{
		this.jdbcTemplate.update(this.sqlService.getSql("userDeleteAll"));
	}
	
	public User get(String id) throws ClassNotFoundException, SQLException{
		return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGet"),
			new Object[] {id}, this.userMapper);
	}

	public int getCount() throws SQLException{
		return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGetCount"), Integer.class);
	}
	
	public List<User> getAll() {
		return this.jdbcTemplate.query(this.sqlService.getSql("userGetAll"), this.userMapper);
	}

	@Override
	public void update(User user) {
		this.jdbcTemplate.update(
				this.sqlService.getSql("userUpdate"), user.getName(),user.getPassword(),
				user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getEmail(), user.getId());

		
	}
	
}
