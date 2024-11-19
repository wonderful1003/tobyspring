package springbook.user.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;

public interface DataSource extends Wrapper{
	Connection getConnection() throws SQLException;
}
