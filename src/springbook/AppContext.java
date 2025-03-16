package springbook;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.hsqldb.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
@Import(SqlServiceContext.class)
public class AppContext {
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
	
	@Configuration
	@Profile("production")
	public static class ProductionAppContext {
		@Bean
		public MailSender mailSender() {
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setHost("localhost");
			return mailSender;
		}
	}
	
	@Configuration
	@Profile("test")
	public static class TestAppContext {
		@Bean
		public UserService testUserService() {
			return new TestUserService();
		}
		@Bean
		public MailSender mailSender() {
			return new DummyMailSender();
		}
	}
}
