package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/**/user/applicationContext.xml")
@Transactional
@TransactionConfiguration(defaultRollback = false)
public class UserServiceTest {
	@Autowired 	UserService userService;
	@Autowired	UserService testUserService;
	@Autowired	UserDao userDao;
	@Autowired	MailSender mailSender;
	@Autowired	PlatformTransactionManager transactionManager;
	@Autowired	ApplicationContext context;
	
	List<User> users;
	
//	@Autowired
//	DataSource dataSource;
	
	//@Autowired
//	UserServiceImpl userServiceImpl;
	
	
	
	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER+1, 0, "a@a.com"),
				new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "b@b.com"),
				new User("erwins", "신승한", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "c@c.com"),
				new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "d@d.com"),
				new User("green", "오민규", "p5", Level.GOLD, 100, 100, "e@e.com")
		);
	}
	
	@Test
	public void upgradeLevels() throws Exception {
		
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "joytouch", Level.SILVER);
		checkUserAndLevel(updated.get(1), "madnite1", Level.GOLD);
		
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}
	
	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}
	
	private void checkLevel(User user, Level expectedLevel) throws ClassNotFoundException, SQLException {
		User userUpdate = userDao.get(user.getId());
		assertThat(userUpdate.getLevel(), is(expectedLevel));
	}

	private void checkLevelUpgraded(User user, boolean upgraded) throws ClassNotFoundException, SQLException {
		User userUpdate = userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	@Test
	public void add() throws ClassNotFoundException, SQLException {
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}

	//@Test(expected=TransientDataAccessResourceException.class)
	public void readOnlyTransactionAttribute(){
		testUserService.getAll();
	}
	
	//@Test 
	@Transactional(readOnly=true)
	@Rollback(false)
	public void transactionSync() throws ClassNotFoundException, SQLException {
		userDao.deleteAll();
		userService.add(users.get(0)); 
		userService.add(users.get(1));
	}

	static class TestUserServiceImpl extends UserServiceImpl{
		private String id = "madnite1";
//		private String id = "";
		
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
		
		public List<User> getAll() {
			for(User user : super.getAll()) {
				super.update(user);
			}
			return null;
		}
	}
	
	static class TestUserServiceException extends RuntimeException{
		
	}
	
	@Test
	@DirtiesContext
	public void upgradeAllOrNothing() throws Exception {
		
		userDao.deleteAll();
		for(User user : users) userDao.add(user);
		
		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
			// TODO: handle exception
		}
		
		checkLevelUpgraded(users.get(1), false);
	}
	
	//@Test
	public void mockUpgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		UserDao mockUserDao = mock(UserDao.class);
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		verify(mockUserDao, times(2)).update(any(User.class)); 
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1)); 
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		ArgumentCaptor<SimpleMailMessage> mailMessageArg = 
				ArgumentCaptor.forClass(SimpleMailMessage.class); 
		
		verify(mockMailSender, times(2)).send(mailMessageArg.capture()); 
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues(); 
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail())); 
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail())) ;
		
	}
	
	static class MockMailSender implements MailSender{
		
		private List<String> requests = new ArrayList<String>();
				
		private List<String> getRequests() {
			return requests;
		}
		
		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}
		
		public void send(SimpleMailMessage[] mailMessage) throws MailException{
			
		}
	}
	
	static class MockUserDao implements UserDao{

		private List<User> users;
		private List<User> updated = new ArrayList();
		
		private MockUserDao(List<User> users) {
			this.users = users;
		}

		public List<User> getUpdated() {
			return this.updated;
		}

		@Override
		public List<User> getAll() {
			return this.users;
		}

		@Override
		public void update(User user) {
			updated.add(user);
		}
		
		@Override
		public void add(User user) throws UnsupportedOperationException {
		}
		
		@Override
		public User get(String id) throws UnsupportedOperationException {
			return null;
		}
		
		@Override
		public void deleteAll() throws UnsupportedOperationException {
		}
		
		@Override
		public int getCount() throws UnsupportedOperationException {
			return 0;
		}
	}
}
