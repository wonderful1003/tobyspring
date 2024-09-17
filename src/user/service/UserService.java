package user.service;

import java.sql.SQLException;

import user.domain.User;

public interface UserService {
	void add(User user) throws ClassNotFoundException, SQLException;
	void upgradeLevels();
}
