package user.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import user.domain.User;

@Transactional
public interface UserService {
	void add(User user) throws ClassNotFoundException, SQLException;
	void deleteAll() throws ClassNotFoundException, SQLException;;
	void update(User user);
	void upgradeLevels();
	
	@Transactional(readOnly=true)
	User get(String id) throws ClassNotFoundException, SQLException;
	
	@Transactional(readOnly=true)
	List<User> getAll();
	
	
}
