package user.service;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

public class UserService {

	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
		
	
	UserDao userDao;
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public void upgradeLevels() {
		List<User> users = userDao.getAll();
		for(User user : users) {
			Boolean changed = null;
			if(user.getLevel() == Level.BASIC && user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER){
				user.setLevel(Level.SILVER);
				changed = true;
			}else if(user.getLevel() == Level.SILVER && user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD) {
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

	protected void upgradeLevel(User user) {
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
		case BASIC: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
		case SILVER: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
		case GOLD: return false;
		default: throw new IllegalArgumentException("Unknown Level : "+currentLevel);
		}
	}
}
