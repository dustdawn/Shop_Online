package com.dustdawn.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dustdawn.dao.ProductDao;
import com.dustdawn.entity.Category;
import com.dustdawn.entity.Order;
import com.dustdawn.entity.OrderItem;
import com.dustdawn.entity.PageBean;
import com.dustdawn.entity.Product;
import com.dustdawn.entity.User;
import com.dustdawn.utils.DataSourceUtils;

public class ProductService {

	public List<Product> findHotProductList() {
		ProductDao dao = new ProductDao();
		List<Product> hotProductList = null;
		try {
			hotProductList = dao.findHotProductList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotProductList;
	}

	public List<Product> findNewProductList() {
		ProductDao dao = new ProductDao();
		List<Product> newProductList = null;
		try {
			newProductList = dao.findNewProductList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newProductList;
	}

	public List<Category> findAllCategory() {
		ProductDao dao = new ProductDao();
		List<Category> categoryList = null;
		try {
			categoryList = dao.findAllCategory();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryList;
	}

	public PageBean findProductListByCid(String cid, int currentPage, int currentCount) {
		ProductDao dao = new ProductDao();
		PageBean<Product> pageBean = new PageBean<Product>();
		
		
		pageBean.setCurrentPage(currentPage);
		pageBean.setCurrentCount(currentCount);
		
		int totalCount = 0;
		try {
			totalCount= dao.getCount(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pageBean.setTotalCount(totalCount);
		
		int totalPage = (int) Math.ceil(1.0*totalCount/currentCount);
		
		
		pageBean.setTotalPage(totalPage);
		
		int index=(currentPage-1)*currentCount;
		List<Product> list = null;
		try {
			list = dao.findProductListByPage(cid,index,currentCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pageBean.setList(list);
		return pageBean;
	}

	public Product findProductByPid(String pid) {
		ProductDao dao = new ProductDao();
		Product product =null;
		try {
			product = dao.findProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return product;
	}
	
	//提交订单和数据和订单项的数据存储到数据库
	public void submitOrder(Order order) {
		ProductDao dao = new ProductDao();
		try {
			//1.开启事务
			DataSourceUtils.startTransaction();
			//2.调用dao存储order表数据的方法
			dao.addOrders(order);
			
			//3.调用dao存储orderitem表数据的方法
			dao.addOrderItem(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				DataSourceUtils.commitAndRelease();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateOrderAdrr(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderAdrr(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateOrderState(String r6_Order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderState(r6_Order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//获得指定的订单集合
	public List<Order> findAllOrders(String uid) {
		ProductDao dao = new ProductDao();
		List<Order> orderList = null;
		try {
			orderList = dao.findAllOrders(uid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderList;
	}

	public List<Map<String, Object>> findAllOrderItemByOid(String oid) {
		ProductDao dao = new ProductDao();
		List<Map<String, Object>> mapList = null;
		try {
			mapList = dao.findAllOrderItemByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mapList;
		
	}
	
}
