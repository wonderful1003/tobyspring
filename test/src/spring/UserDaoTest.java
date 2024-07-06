package spring;

import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {

	private UserDao dao;

	private User user1; 
	private User user2; 
	private User user3; 
			
	@Before
	public void setUp() throws SQLException, ClassNotFoundException{
		
		ApplicationContext context = 
				new GenericXmlApplicationContext("/**/applicationContext.xml");
		this.dao = context.getBean("userDao", UserDao.class);
		
		this.user1 = new User("wonderful121" , "방승현", "married");
		this.user2 = new User("wonderful122" , "방승현1", "married");
		this.user3 = new User("wonderful123" , "방승현2", "married");

	}
	
	
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException{
		
		dao.deleteAll();
		Assert.assertEquals(dao.getCount(), 0);
		
		dao.add(user1);
		dao.add(user2);
		Assert.assertEquals(dao.getCount(), 2);
		
		User userget1 = dao.get(user1.getId());
		Assert.assertEquals(userget1.getName(), "방승현");
		Assert.assertEquals(userget1.getPassword(), "married");
		
		User userget2 = dao.get(user2.getId());
		Assert.assertEquals(userget2.getName(), "방승현1");
		Assert.assertEquals(userget2.getPassword(), "married");
		
		
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		System.out.println(user2.getId()+"조회성공");
		
//		assertThat(user.getName(), equals(user.getName()));
//		assertThat(user.getName(), user.getName());
		
//		if(!user.getName().equals(user2.getName())){
//			System.out.println("테스트실패 (name)");	
//		}else if(!user.getPassword().equals(user2.getPassword())){
//			System.out.println("테스트실패 (password)");
//		}else {
//			System.out.println("테스트성공");
//		}
	}

	@Test
	public void count() throws SQLException, ClassNotFoundException {
		
		dao.deleteAll();
		Assert.assertEquals(dao.getCount(), 0);
		
		
		dao.add(user1);
		Assert.assertEquals(dao.getCount(), 1);

		dao.add(user2);
		Assert.assertEquals(dao.getCount(), 2);
		
		dao.add(user3);
		Assert.assertEquals(dao.getCount(), 3);
	}
	

//	private void assertThat(String name, String name2) {
//		// TODO Auto-generated method stub
//		
//	}
	
//	public static void main(String[] args) throws ClassNotFoundException, SQLException{
//		
//		ApplicationContext context = 
//				new GenericXmlApplicationContext("/**/applicationContext.xml");
//		UserDao dao = context.getBean("userDao", UserDao.class);
//		
//		User user = new User();
//		user.setId("wonderful322");
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
