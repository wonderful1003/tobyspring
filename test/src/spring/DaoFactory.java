package spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class DaoFactory {
	
	@Bean
	public UserDao userDao() {
//		return new UserDao(connectionMaker());
		UserDao userDao = new UserDao();
		userDao.setDataSource(dataSource());
		return userDao;
	}
	
	@Bean
	public ConnectionMaker connectionMaker() {
		return new LocalDBConnectionMaker();
	}
	
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		
//		dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
//		dataSource.setUrl("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8&serverTimezone=UTC");
//		dataSource.setUsername("root");
//		dataSource.setPassword("qwer!2345");
		
		return dataSource;
	}

}
