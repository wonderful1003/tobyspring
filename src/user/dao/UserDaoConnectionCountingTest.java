package user.dao;

import user.domain.User;
import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoConnectionCountingTest {
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		
		ApplicationContext context = 
				new AnnotationConfigApplicationContext(CountingDaoFactory.class);
	
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		User user = new User();
		user.setId("wonderful");
		user.setName("방승현");
		user.setPassword("married");
		
		dao.add(user);
		dao.add(user);
		CountingConnectionMaker ccm = context.getBean("connectionMaker", CountingConnectionMaker.class);
		
		System.out.println(" Connection counter : "+ccm.getCounter());
		
		
	}
}
