package tobyspring;

import java.sql.SQLException;

public class UserDaoTest {
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		
		ConnectionMaker connectionMaker = new DConnectionMaker();
		
		UserDao dao = new UserDao(connectionMaker);
		
		User user = new User();
		user.setId("wonderful");
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
