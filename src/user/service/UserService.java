package user.service;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

public class UserService {
	
	UserDao userDao;
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public void upgradeLevels() {
		List<User> users = userDao.getAll();
		for(User user : users) {
			Boolean changed = null;
			if(user.getLevel() == Level.BASIC && user.getLogin() >= 50){
				user.setLevel(Level.SILVER);
				changed = true;
			}else if(user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
				user.setLevel(Level.GOLD);
				changed = true;
			}else if(user.getLevel() == Level.GOLD) {
				changed = false;
			}else {
				changed = false;
			}
			
			if(changed) {
				userDao.update(user);
			}
		}
	}

	private void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
	}
	
	public void add(User user) throws ClassNotFoundException, SQLException {
		if(user.getLevel() == null) user.setLevel(Level.BASIC);
		userDao.add(user);
	}
	
	private boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		switch (currentLevel) {
		case BASIC: return (user.getLogin() >= 50);
		case SILVER: return (user.getRecommend() >= 30);
		case GOLD: return false;
		default: throw new IllegalArgumentException("Unknown Level : "+currentLevel);
		}
	}
}
