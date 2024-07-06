package spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;

import javax.sql.CommonDataSource;

public interface DataSource extends CommonDataSource,Wrapper{
	Connection geConnection() throws SQLException;
}
