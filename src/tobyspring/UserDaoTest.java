package tobyspring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
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
		
		dao.deleteAll(); 
		assertThat(dao.getCount(), is(0)); 

		User user3 = new User(); 
		user3.setId( "gyumee"); 
		user3.setName(" 박성 철 "); 
		user3.setPassword( "springno1 "); 
		
		dao.add(user3); 
		assertThat(dao.getCount(), is(1));
		
		User user4 = dao.get(user3.getId()); 
		assertThat(user4.getName() , is(user4.getName())); 
		assertThat(user4.getPassword() , is(user4.getPassword()));
		
	}
	
	@Test 
	public void count() throws SQLException, ClassNotFoundException { 
		ApplicationContext context = new GenericXmlApplicationContext(
				"/**/applicationContext.xml");
		
		UserDao dao = context.getBean("userDao" , UserDao.class); 
		User userl = new User("gyumee", "박성철", "springnol"); 
		User user2 = new User("leegw700", "이길원", "springno2"); 
		User user3 = new User("bumJin", "박범진", "springno3");
				
		dao.deleteAll();
		
		assertThat(dao.getCount() , is(0)); 

		dao.add(userl);
		assertThat(dao.getCount(), is(1)); 
		
		dao.add(user2);
		assertThat(dao.getCount() , is(2)); 

		dao.add(user3);
		assertThat(dao.getCount() , is(3));
	}
}
