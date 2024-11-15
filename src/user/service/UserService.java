package user.service;

import java.sql.SQLException;
import java.util.List;

import user.domain.User;

public interface UserService {
	void add(User user) throws ClassNotFoundException, SQLException;
	
	User get(String id) throws ClassNotFoundException, SQLException;;;
	List<User> getAll();
	void deleteAll() throws ClassNotFoundException, SQLException;;
	void update(User user);
	
	void upgradeLevels();
	
}
