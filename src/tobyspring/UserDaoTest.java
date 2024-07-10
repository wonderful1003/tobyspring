package tobyspring;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		
		ApplicationContext context = new GenericXmlApplicationContext(
				"/**/applicationContext.xml");
		
		UserDao dao = context.getBean("userDao", UserDao.class);
		User user = new User();
		user.setId("wonderful");
		user.setName("방승현");
		user.setPassword("married");
		
		dao.add(user);
		
		User user2 = dao.get(user.getId());
		
		Assert.assertEquals(user.getName(), user2.getName());	
		Assert.assertEquals(user.getName(), user2.getName());	
	}
}
