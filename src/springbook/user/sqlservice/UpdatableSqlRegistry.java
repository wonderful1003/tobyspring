package springbook.user.sqlservice;

import java.sql.SQLException;
import java.util.Map;

import springbook.user.sqlservice.SqlRegistry;

public interface UpdatableSqlRegistry extends SqlRegistry{
	public void updateSql(String key, String sql) throws SqlUpdateFailureException;
	
	public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;

}
