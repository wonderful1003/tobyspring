package user.dao;

import java.sql.SQLException;
import java.util.List;

import user.domain.User;

public interface UserDao {
	void add(User user) throws ClassNotFoundException, SQLException;
	User get(String id) throws ClassNotFoundException, SQLException;
	List<User> getAll();
	void deleteAll() throws SQLException, ClassNotFoundException;
	int getCount() throws SQLException;
	public void update(User user1);
		
}
