package user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/**/user/applicationContext.xml")
@DirtiesContext
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context; 
	
	@Autowired
	UserDaoJdbc dao;

	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		this.user1 = new User("gyumee", "박성철", "springnol", Level.BASIC, 1, 0); 
		this.user2 = new User("leegw700", "이길원", "springno2", Level.SILVER, 55, 10); 
		this.user3 = new User("bumJin", "박범진", "springno3", Level.GOLD, 100, 40);
	}
	
	//@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		
		dao.deleteAll(); 
		assertThat(dao.getCount(), is(0)); 
		
		dao.add(user1); 
		dao.add(user2); 
		assertThat(dao.getCount(), is(2));

		User userget1 = dao.get(user1.getId()); 
		checkSameUser(userget1, user1);
		
		User userget2 = dao.get(user2.getId()); 
		checkSameUser(userget2, user2);
		
	}
	
	//@Test 
	public void count() throws SQLException, ClassNotFoundException { 

		User user1 = new User("gyumee", "박성철", "springnol"); 
		User user2 = new User("leegw700", "이길원", "springno2"); 
		User user3 = new User("bumJin", "박범진", "springno3");
			
		dao.deleteAll();
		
		assertThat(dao.getCount() , is(0)); 

		dao.add(user1);
		assertThat(dao.getCount(), is(1)); 
		
		dao.add(user2);
		assertThat(dao.getCount() , is(2)); 

		dao.add(user3);
		assertThat(dao.getCount() , is(3));
	}
	
	//@Test(expected=EmptyResultDataAccessException.class) 
	public void getUserFailure() throws SQLException, ClassNotFoundException{ 

		dao.deleteAll(); 
		assertThat(dao.getCount(), is(0)); 
		
		dao.get("unknown_id");
	}
	
	//@Test
	public void getAll() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		
		dao.add(user1);
		List<User> user1 = dao.getAll();	//gyumee
		assertThat(user1.size() , is(1));
		checkSameUser(this.user1, user1.get(0));
		
		dao.add(user2);
		List<User> user2 = dao.getAll();	//leegw700
		assertThat(user2.size() , is(2));
		checkSameUser(this.user1, user2.get(0));
		checkSameUser(this.user2, user2.get(1));

		dao.add(user3);						//bumJin
		List<User> user3 = dao.getAll();
		assertThat(user3.size() , is(3));
		checkSameUser(this.user3, user3.get(0));
		checkSameUser(this.user1, user3.get(1));
		checkSameUser(this.user2, user3.get(2));
		
	}
	
	//@Test(expected=DataAccessException.class)
	public void duplicateKey() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);
	}
	
	@Test
	public void update() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user2);
		
		user1.setName("오민규");
		user1.setPassword("springno6");
		user1.setLevel(Level.GOLD);
		user1.setLogin(1000);
		user1.setRecommend(999);
		
		dao.update(user1);
		
		User user1update = dao.get(user1.getId());
		checkSameUser(user1, user1update);
		User user2same = dao.get(user2.getId());
		checkSameUser(user2, user2same);
	}
	
	private void checkSameUser(User user1, User user2) {
		assertThat(user1.getId()  , is(user2.getId()));
		assertThat(user1.getName(), is(user2.getName()));
		assertThat(user1.getPassword()  , is(user2.getPassword()));
		assertThat(user1.getLevel()  , is(user2.getLevel()));
		assertThat(user1.getLogin()  , is(user2.getLogin()));
		assertThat(user1.getRecommend()  , is(user2.getRecommend()));
	}
}
