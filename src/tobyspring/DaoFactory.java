package tobyspring;

public class DaoFactory {
	public UserDao userDao() {
//		ConnectionMaker connectionMaker = new DConnectionMaker();
//		UserDao userDao = new UserDao(connectionMaker);
		return new UserDao(connectionMaker());
	}
	
	public AccountDao accountDao() {
		return new accountDao(connectionMaker());
	}

	public MessageDao messageDao() {
		return new MessageDao(connectionMaker());
	}
	
	public connectionMaker connectionMaker() {
		return new DconnectionMaker();
	}
}
