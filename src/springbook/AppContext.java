package springbook;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.hsqldb.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.mysql.jdbc.Driver;

import springbook.user.dao.UserDao;
import springbook.user.service.DummyMailSender;
import springbook.user.service.UserService;
import springbook.user.sqlservice.SqlRegistry;
import springbook.user.sqlservice.SqlService;
import springbook.user.dao.UserDaoJdbc;
import springbook.user.service.UserServiceImpl;
import springbook.user.service.UserServiceTest.TestUserService;
import springbook.user.sqlservice.OxmSqlService;
import springbook.user.sqlservice.SqlRegistry;
import springbook.user.sqlservice.updatable.EmbeddedDbSqlRegistry;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "springbook.user")
public class AppContext {

	@Autowired UserDao userDao;
	
	@Bean
	public DataSource dataSource() {
		
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		
		dataSource.setDriverClass(Driver.class);
		dataSource.setUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=UTF-8");
		dataSource.setUsername("root");
		dataSource.setPassword("qwer!2345");
		
		return dataSource;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		
		DataSourceTransactionManager tm = new DataSourceTransactionManager();
		tm.setDataSource(dataSource());
		return tm;
	}
	
	@Bean 
	public UserDao userDao() {
		return new UserDaoJdbc();
	}
	
	/**
	 * SQL서비스
	 */
	@Bean
	public SqlService sqlService() {
		OxmSqlService sqlService = new OxmSqlService();
		sqlService.setUnmarshaller(unmarshaller());
		sqlService.setSqlRegistry(sqlRegistry());
		return sqlService;
	}
	
	@Bean
	public SqlRegistry sqlRegistry() {
		EmbeddedDbSqlRegistry sqlRegistry = new EmbeddedDbSqlRegistry();
		sqlRegistry.setDataSource(embeddedDatabase());
		return sqlRegistry;
	}
	
	@Bean
	public Unmarshaller unmarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("springbook.user.sqlservice.jaxb");
		return marshaller;
	}
	
	@Bean 
	public DataSource embeddedDatabase() {
		return new EmbeddedDatabaseBuilder()
			.setName("embeddedDatabase")
			.setType(HSQL)
			.addScript("classpath:springbook/user/sqlservice/updatable/sqlRegistrySchema.sql")
			.build();
	}	
}
