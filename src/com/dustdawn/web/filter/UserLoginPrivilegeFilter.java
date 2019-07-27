package com.dustdawn.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dustdawn.entity.User;

public class UserLoginPrivilegeFilter implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpSession session = req.getSession();
		//判断用户是否登陆
		User user = (User)session.getAttribute("user");
		if(user==null) {
			resp.sendRedirect(req.getContextPath()+"/login.jsp");
			return;
		}
		chain.doFilter(req, resp);
	}
	
	
}
