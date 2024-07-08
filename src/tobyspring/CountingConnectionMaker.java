package tobyspring;

import java.sql.Connection;
import java.sql.SQLException;

public class CountingConnectionMaker implements ConnectionMaker{

	int counter = 0;
	private ConnectionMaker realConnectionMaker; 
	
	public CountingConnectionMaker(ConnectionMaker reConnectionMaker) {
		this.realConnectionMaker = reConnectionMaker;
	}
	
	@Override
	public Connection makeConnection() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		this.counter++;
		return realConnectionMaker.makeConnection();
	}
	
	public int getCounter() {
		return this.counter;
	}
}
