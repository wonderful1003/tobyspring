package user.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import user.dao.UserDao;
import user.domain.Level;
import user.domain.User;

public class UserService {

	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
		
	UserDao userDao;
	
//	private DataSource dataSource;
//		
//	public void setDataSource(DataSource dataSource) {
//		this.dataSource = dataSource;
//	}
	
	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public void upgradeLevels() throws Exception {
		
		TransactionStatus status = 
				this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			List<User> users = userDao.getAll();
			for(User user : users) {
				if(canUpgradeLevel(user)) {
					upgradeLevel(user);
				}
			}
			this.transactionManager.commit(status);
		}catch (Exception e) {
			this.transactionManager.rollback(status);
			throw e;
		}finally {
//			DataSourceUtils.releaseConnection(c, dataSource);
//			TransactionSynchronizationManager.unbindResource(this.dataSource);
//			TransactionSynchronizationManager.clearSynchronization();
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
