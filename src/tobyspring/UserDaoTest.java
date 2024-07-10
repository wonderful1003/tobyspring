package tobyspring;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		
		ApplicationContext context = 
				new GenericXmlApplicationContext("/**/applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		User user = new User();
		user.setId("wonderful");
		user.setName("방승현");
		user.setPassword("married");
		
		dao.add(user);
		
		System.out.println(user.getId() + " success");
		
		User user2 = dao.get(user.getId());
		
		if (!user.getName().equals(user2.getName())) { 
			System.out.println(" 테스트 실패 (name)"); 
		}else if (!user.getPassword().equals(user2.getPassword())) { 
			System.out.println(" 테스트 실패 (password)"); 
		}	else { 
			System.out.println("조회 테스트 성공");
		}			
	}
}
