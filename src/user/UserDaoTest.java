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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import user.dao.UserDao;
import user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/**/user/applicationContext.xml")
@DirtiesContext
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context; 
	
	@Autowired
	UserDao dao;

	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		this.user1 = new User("gyumee", "박성철", "springnol"); 
		this.user2 = new User("leegw700", "이길원", "springno2"); 
		this.user3 = new User("bumJin", "박범진", "springno3");
	}
	
	//@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		
		dao.deleteAll(); 
		assertThat(dao.getCount(), is(0)); 
		
		dao.add(user1); 
		dao.add(user2); 
		assertThat(dao.getCount(), is(2));

		User userget1 = dao.get(user1.getId()); 
		assertThat(userget1.getName(), is(user1.getName())); 
		assertThat(userget1.getPassword(), is(user1.getPassword())); 

		User userget2 = dao.get(user2.getId()); 
		assertThat(userget2.getName(), is(user2.getName())); 
		assertThat(userget2.getPassword(), is(user2.getPassword()));
		
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
	
	@Test
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
	
	private void checkSameUser(User user1, User user2) {
		assertThat(user1.getId()  , is(user2.getId()));
		assertThat(user1.getName(), is(user2.getName()));
		assertThat(user1.getPassword()  , is(user2.getPassword()));
	}
}
