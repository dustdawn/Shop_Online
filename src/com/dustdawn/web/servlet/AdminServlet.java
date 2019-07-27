package com.dustdawn.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;

import com.dustdawn.entity.Product;
import com.dustdawn.entity.Category;
import com.dustdawn.entity.Order;
import com.dustdawn.service.AdminService;
import com.dustdawn.vo.Condition;
import com.google.gson.Gson;

public class AdminServlet extends BaseServlet {

	public void findAllCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//提供一个List<Category>转成json字符串
		AdminService service = new AdminService();
		List<Category> categoryList = service.findAllCategory();
		
		Gson gson = new Gson();
		String json = gson.toJson(categoryList);
		
		response.setContentType("text/json;charset=UTF-8");
		
		response.getWriter().write(json);
		
	}
	//获得所有订单
	public void findAllOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AdminService service = new AdminService();
		List<Order> orderList = service.findAllOrders();
		
		request.setAttribute("orderList", orderList);
		
		request.getRequestDispatcher("/admin/order/list.jsp").forward(request, response);
	}
	
	
	//根据订单项查询订单项和商品信息
	public void findOrderInfoByOid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String oid = request.getParameter("oid");
		AdminService service = new AdminService();
		List<Map<String,Object>> mapList = service.findOrderInfoByOid(oid);
		
		Gson gson = new Gson();
		String json = gson.toJson(mapList);
		
		response.setContentType("text/json;charset=UTF-8");
		response.getWriter().write(json);
	}
	
	
	public void findAllProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AdminService service = new AdminService();
		List<Product> productList = service.findAllProduct();

		request.setAttribute("productList", productList);
		List<Category> categoryList = service.findAllCategory();
		
		request.setAttribute("categoryList", categoryList);
		request.getRequestDispatcher("/admin/product/list.jsp").forward(request, response);
				
		
	}
	
	public void updateProductUI(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pid = request.getParameter("pid");
		//传递pid查询商品信息
		AdminService service = new AdminService();
		Product product = service.findProductByPid(pid);
		
		
		List<Category> categoryList = service.findAllCategory();
		
		request.setAttribute("categoryList", categoryList);
		
		request.setAttribute("product", product);
		request.getRequestDispatcher("/admin/product/edit.jsp").forward(request, response);
		
	}
	
	
	
	
	
	public void SearchProductList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		//收集表单数据
		Map<String, String[]> parameterMap = request.getParameterMap();
		Condition condition = new Condition();
		try {
			BeanUtils.populate(condition, parameterMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		//将实体传递给service
		AdminService service = new AdminService();
		List<Product> productList = service.findProductListByCondition(condition);
		
		
		List<Category> categoryList = service.findAllCategory();
		
		request.setAttribute("condition", condition);
		request.setAttribute("categoryList", categoryList);
		
		
		request.setAttribute("productList", productList);
		request.getRequestDispatcher("/admin/product/list.jsp").forward(request, response);
	}
	
	
	public void delProductByPid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String pid = request.getParameter("pid");
		 
		//传递pid到service层
		AdminService service = new AdminService();
		service.delProductByPid(pid);
		
		
		response.sendRedirect(request.getContextPath()+"/admin?method=findAllProduct");
		
	}
	
	public void addProductUI(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		AdminService service = new AdminService();
		List<Category> categoryList = service.findAllCategory();
	
		request.setAttribute("categoryList", categoryList);
		request.getRequestDispatcher("/admin/product/add.jsp").forward(request, response);
	}

	
	
	

	

	
}