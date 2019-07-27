package com.dustdawn.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.dustdawn.entity.User;
import com.dustdawn.service.UserService;
import com.dustdawn.utils.CommonsUtils;
import com.dustdawn.utils.MailUtils;

public class RegisterServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		Map<String, String[]> properties = request.getParameterMap();
		User user = new User();
		try {
			ConvertUtils.register(new Converter() {
				@Override
				public Object convert(Class clazz, Object value) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date parse = null;
					try {
						 parse = format.parse(value.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return parse;
				}
			}, Date.class);
			BeanUtils.populate(user, properties);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		user.setUid(CommonsUtils.getUUID());
		user.setTelephone(null);
		user.setState(0);
		
		String activeCode = CommonsUtils.getUUID();
		user.setCode(activeCode);
		
		UserService service = new UserService();
		boolean isRegisterSuccess = false;
		try {
			isRegisterSuccess = service.regist(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(isRegisterSuccess) {
			//发送激活邮件
			String emailMsg = "恭喜您注册成功，请点击下面的连接进行激活账户"
					+ "<a href='http://localhost:80/Shop_Online/active?activeCode="+activeCode+"'>"
							+ "http://localhost:80/Shop_Online/active?activeCode="+activeCode+"</a>";
			try {
				MailUtils.sendMail(user.getEmail(), emailMsg);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
		}else {
			response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}