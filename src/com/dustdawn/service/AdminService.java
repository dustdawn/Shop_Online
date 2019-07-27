package com.dustdawn.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dustdawn.dao.AdminDao;

import com.dustdawn.entity.Category;
import com.dustdawn.entity.Order;
import com.dustdawn.entity.Product;
import com.dustdawn.vo.Condition;

public class AdminService {

	public List<Category> findAllCategory() {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.findAllCategory();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void saveProduct(Product product) {
		AdminDao dao = new AdminDao();
		
		try {
			dao.saveProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Order> findAllOrders() {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.findAllOrders();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public List<Map<String, Object>> findOrderInfoByOid(String oid) {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.findOrderInfoByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Product> findAllProduct() {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.findAllProduct();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Product updateProductUI(String pid) {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.updateProductUI(pid);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Product> findProductListByCondition(Condition condition) {
		AdminDao dao = new AdminDao();
		
		try {
			return dao.findProductListByCondition(condition);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void delProductByPid(String pid) {
		AdminDao dao = new AdminDao();
		try {
			dao.delProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
	}

	public Product findProductByPid(String pid) {
		AdminDao dao = new AdminDao();
		try {
			return dao.findProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateProduct(Product product) {
		AdminDao dao = new AdminDao();
		try {
			dao.updateProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}
	
}
