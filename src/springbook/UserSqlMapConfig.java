package springbook;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import springbook.user.dao.UserDao;

public class UserSqlMapConfig implements SqlMapConfig{
	@Override
	public Resource getSqlMapResouce() {
		return new ClassPathResource("sqlmap.xml", UserDao.class);
	}
}
