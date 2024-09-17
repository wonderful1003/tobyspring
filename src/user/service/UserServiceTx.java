package user.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import user.domain.User;

public class UserServiceTx implements UserService{

	UserService userService;
	PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public void add(User user) throws ClassNotFoundException, SQLException {
		this.userService.add(user);	
	}

	@Override
	public void upgradeLevels() {
		TransactionStatus status = this.transactionManager
				.getTransaction(new DefaultTransactionDefinition());
		try {
			userService.upgradeLevels();
			this.transactionManager.commit(status);
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
		
	}
	
}
