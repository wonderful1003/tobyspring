package springbook.user.sqlservice;

import java.sql.SQLException;

public interface SqlRegistry {
	
	void registerSql(String key, String sql);
	String findSql(String key) throws SQLException;
}
