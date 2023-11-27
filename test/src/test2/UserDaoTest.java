package test2;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		
		ApplicationContext context = 
				new GenericXmlApplicationContext("/**/applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		User user = new User();
		user.setId("wonderful133322");
		user.setName("방승현");
		user.setPassword("married");
		
		dao.add(user);
		
		System.out.println(user.getId() + " success");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		System.out.println(user2.getId()+"조회성공");
		
	}
}
