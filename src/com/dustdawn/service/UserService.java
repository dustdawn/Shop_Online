package com.dustdawn.service;

import java.sql.SQLException;

import com.dustdawn.dao.UserDao;
import com.dustdawn.entity.User;

public class UserService {

	public boolean regist(User user) throws SQLException {
		UserDao dao = new UserDao();
		int row = dao.regist(user);

		return row>0?true:false;
	}
	//激活
	public void active(String activeCode) {
		UserDao dao = new UserDao();
		try {
			dao.active(activeCode);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public boolean checkUsername(String username) {
		UserDao dao = new UserDao();
		Long isExist = 0L;
		try {
			isExist = dao.checkUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return isExist>0?true:false;
	}
	//用户登录的方法
	public User login(String username, String password) throws SQLException {
		UserDao dao = new UserDao();
		return dao.login(username,password);
	}
}
