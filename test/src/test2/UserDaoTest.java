package test2;

import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {

	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException{
		
		ApplicationContext context = 
				new GenericXmlApplicationContext("/**/applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		User user = new User();
		user.setId("wonderful121");
		user.setName("방승현");
		user.setPassword("married");
		
		dao.add(user);
		
		System.out.println(user.getId() + " success");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		System.out.println(user2.getId()+"조회성공");
		
		Assert.assertEquals(user.getName(), user2.getName());
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
