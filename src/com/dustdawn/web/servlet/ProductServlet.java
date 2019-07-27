package com.dustdawn.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.dustdawn.entity.Cart;
import com.dustdawn.entity.CartItem;
import com.dustdawn.entity.Category;
import com.dustdawn.entity.Order;
import com.dustdawn.entity.OrderItem;
import com.dustdawn.entity.PageBean;
import com.dustdawn.entity.Product;
import com.dustdawn.entity.User;
import com.dustdawn.service.ProductService;
import com.dustdawn.utils.CommonsUtils;
import com.dustdawn.utils.JedisPoolUtils;
import com.dustdawn.utils.PaymentUtil;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

public class ProductServlet extends BaseServlet {

	/*public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		//获得请求的哪个方法
		String methodName = request.getParameter("method");
		if("productList".equals(methodName)) {
			productList(request,response);
		}else if("categoryList".equals(methodName)) {
			categoryList(request,response);
		}else if("productInfo".equals(methodName)) {
			productInfo(request,response);
		}else if("index".equals(methodName)) {
			index(request,response);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}*/
	
	//模块中的功能是通过方法进行区分的
	
	//显示商品的类别的功能CategoryListServlet
	public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		
		//从缓存中查询categoryList，如果有直接使用，没有再从数据库中查询，存到缓存中
		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		if(categoryListJson==null) {
			System.out.println("缓存没有数据 查询数据库");
			List<Category> categoryList = service.findAllCategory();
			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);
			jedis.set("categoryListJson",categoryListJson);
		}
		//准备分类数据

		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(categoryListJson);
	}
	
	//显示首页功能IndexServlet
	public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		List<Product> hotProductList = service.findHotProductList();
		List<Product> newProductList = service.findNewProductList();
		
		
		request.setAttribute("hotProductList",hotProductList );
		request.setAttribute("newProductList",newProductList );
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	
	
	//显示商品的详细信息ProductInfoServlet
	public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pid = request.getParameter("pid");
		//获得商品类别，返回用
		String currentPage = request.getParameter("currentPage");
		String cid = request.getParameter("cid");
		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);
		
		//历史
		
		
		request.setAttribute("product",product);
		request.setAttribute("cid",cid);
		request.setAttribute("currentPage",currentPage);
		
		//获得客户端携带的cookie--获得名字是pids的cookie、
		String pids = pid;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie cookie : cookies) {
				if("pids".equals(cookie.getName())) {
					pids = cookie.getValue();
					//1-2-3本次访问商品pid是8---->8-1-2-3
					//1-2-3本次访问商品pid是3---->3-1-2
					//1-2-3本次访问商品pid是2---->2-1-3
					//将pids拆成一个数组
					String[] split = pids.split("-");
					List<String> asList = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<String>(asList);
					if(list.contains(pid)) {
						//包含当前查看商品的pid
						list.remove(pid);
						list.addFirst(pid);
					}else {
						list.addFirst(pid);
					}
					//将[1,2,3]转成字符串
					StringBuffer sb = new StringBuffer();
					for(int i=0;i<list.size()&&i<7;i++) {
						sb.append(list.get(i));
						sb.append("-");//1-2-3-
					}
					pids = sb.substring(0, sb.length()-1);//1-2-3
					
				}
			}
		}
		Cookie cookie_pids = new Cookie("pids",pids);
		response.addCookie(cookie_pids);
		
		
		
		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}
	
	//根据商品的类别获得商品的列表ProductListByCid
	public void productList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cid = request.getParameter("cid");
		String currentPageStr = request.getParameter("currentPage");
		if(currentPageStr==null) currentPageStr="1";
		int currentPage = Integer.parseInt(currentPageStr);
		int currentCount = 12;
		
		ProductService service = new ProductService();
		PageBean pageBean = service.findProductListByCid(cid,currentPage,currentCount);
		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);
		
		
		//定义一个集合记录历史消息
		List<Product> historyProductList = new ArrayList<Product>();
		//获得客户端携带的名为pids的cookie
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie cookie : cookies) {
				if("pids".equals(cookie.getName())) {
					String pids = cookie.getValue();
					String[] split = pids.split("-");
					for(String pid : split) {
						Product pro = service.findProductByPid(pid);
						historyProductList.add(pro);
					}
				}
			}
		}
		request.setAttribute("historyProductList", historyProductList);
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);
	}
	
	//商品添加到购物车
	public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		ProductService service = new ProductService();
		String pid = request.getParameter("pid");
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));
		Product product = service.findProductByPid(pid);
		double subtotal = product.getShop_price()*buyNum;
		//封装CartItem
		CartItem item = new CartItem();
		item.setProduct(product);
		item.setBuyNum(buyNum);
		item.setSubtotal(subtotal);
		//获得购物车--判断是否已经存在购物车
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart==null) {
			cart = new Cart();
		}
		
		//放入购物车key是pid
		//如果购物车中已存在该商品,cartItem替换item
		Map<String, CartItem> cartItems = cart.getCartItems();
		double newsubtotal = 0.0;
		if(cartItems.containsKey(pid)) {
			CartItem cartItem = cartItems.get(pid);
			int oldBuyNum = cartItem.getBuyNum();
			oldBuyNum+=buyNum;
			cartItem.setBuyNum(oldBuyNum);
			cart.setCartItems(cartItems);
			//修改小计
			double oldsubtotal = cartItem.getSubtotal();
			newsubtotal = buyNum*product.getShop_price();
			cartItem.setSubtotal(newsubtotal+oldsubtotal);
			
		}else {
			cart.getCartItems().put(product.getPid(),item);
			newsubtotal = buyNum*product.getShop_price();
		}
		
		
		double total = cart.getTotal()+newsubtotal;
		cart.setTotal(total);
		
		

		
		//将车再次访问sisson
		session.setAttribute("cart", cart);
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	
	
	//删除单一产品
	public void delProFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//获得删除的item的pid
		String pid = request.getParameter("pid");
		HttpSession session = request.getSession();
		Cart cart = (Cart)session.getAttribute("cart");
		if(cart!=null) {
			Map<String,CartItem> cartItems = cart.getCartItems();
			//需要修改总价
			cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
			//删除
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
			
			
		}
		session.setAttribute("cart", cart);
		//跳转回购物车页
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
		
	}
	
	
	//清空购物车
	public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.removeAttribute("cart");
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
		
	}
	
	
	
	
	
	//订单
	//提交订单
	public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		User user = (User)session.getAttribute("user");
		if(user==null) {
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		
		//封装好一个order对象
		Order order = new Order();
		//private String oid;//该订单的订单号
		String oid = CommonsUtils.getUUID();
		order.setOid(oid);
		
		//private Date ordertime;//下单时间
		order.setOrdertime(new Date());
		
		//private double total;//该订单的总金额
		Cart cart = (Cart) session.getAttribute("cart");
		double total = cart.getTotal();
		order.setTotal(total);
		
		//private int state;//订单支付状态 1代表已付款 0代表未付款
		order.setState(0);
		
		//private String address;//收货地址
		order.setAddress(null);
		
		//private String name;//收货人
		order.setName(null);
		
		//private String telephone;//收货人电话
		order.setTelephone(null);
		
		//private User user;//该订单属于哪个用户
		order.setUser(user);
		
		Map<String, CartItem> cartItems = cart.getCartItems();
		for(Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
			CartItem cartItem = entry.getValue();
			OrderItem orderItem = new OrderItem();
			orderItem.setItemid(CommonsUtils.getUUID());
			orderItem.setCount(cartItem.getBuyNum());
			orderItem.setSubtotal(cartItem.getSubtotal());
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setOrder(order);
			
			order.getOrderItems().add(orderItem);
		}
		//封装完毕
		//传递到service
		ProductService service = new ProductService();
		service.submitOrder(order);
		
		
		session.setAttribute("order", order);
		//跳页
		response.sendRedirect(request.getContextPath()+"/order_info.jsp");
	}	
	
	//确定订单--更新收货人信息+在线支付 
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Order order = new Order();
		//1.更新收货人信息
		try {
			BeanUtils.populate(order, parameterMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		ProductService service = new ProductService();
		service.updateOrderAdrr(order);
		
		//2.在线支付
		//获得选择的银行
		//String pd_FrpId = request.getParameter("pd_FrpId");
		/*if(pd_FrpId.equals("ABC-NET-B2C")) {
			//农业银行
		}else if(pd_FrpId.equals("ICBC-NET-B2C")) {
			//工商银行
		}*/
		//只接入一个接口，这个接口集合所有的银行接口，这个接口是第三方支付平台提供的
		//易宝支付
		
		String orderid = request.getParameter("oid");
		//String money = order.getTotal()+"";
		String money = "0.01";
		// 银行
		String pd_FrpId = request.getParameter("pd_FrpId");

		// 发给支付公司需要哪些数据
		String p0_Cmd = "Buy";
		String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
		String p2_Order = orderid;
		String p3_Amt = money;
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
		// 第三方支付可以访问网址
		String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		// 加密hmac 需要密钥
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
				"keyValue");
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
				p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
				pd_FrpId, pr_NeedResponse, keyValue);
		
		
		String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
						"&p0_Cmd="+p0_Cmd+
						"&p1_MerId="+p1_MerId+
						"&p2_Order="+p2_Order+
						"&p3_Amt="+p3_Amt+
						"&p4_Cur="+p4_Cur+
						"&p5_Pid="+p5_Pid+
						"&p6_Pcat="+p6_Pcat+
						"&p7_Pdesc="+p7_Pdesc+
						"&p8_Url="+p8_Url+
						"&p9_SAF="+p9_SAF+
						"&pa_MP="+pa_MP+
						"&pr_NeedResponse="+pr_NeedResponse+
						"&hmac="+hmac;

		//重定向到第三方支付平台
		response.sendRedirect(url);
	}
	
	
	//获得我的订单
	public void myOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		if(user==null) {
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		
		ProductService service = new ProductService();
		//1.查询该用户的订单信息(单表查询orders表)
		//集合中的每一个order对象的数据是不完整的，缺少orderItem
		List<Order> orderList = service.findAllOrders(user.getUid());
		//循环所有与订单，为每个订单填充单项订单信息
		if(orderList!=null) {
			for(Order order : orderList) {
				//获得每一个订单的oid
				String oid = order.getOid();
				//查询该订单的所有订单项----mapList封装的是多个订单项和该订单项商品信息
				List<Map<String, Object>> mapList = service.findAllOrderItemByOid(oid);
				//将mapList转换成List<OrderItem> orderItems
				for(Map<String, Object> map : mapList) {
		
					try {
						//从map中取出count subtotal封装到OrderItem中
						OrderItem orderItem = new OrderItem();
						BeanUtils.populate(orderItem, map);
						//从map中取出pimage pname shop_price封装到Product中
						Product product = new Product();
						BeanUtils.populate(product, map);
						//将product封装到OrderItem中
						orderItem.setProduct(product);
						//将orderItem封装到order中的orderItemList中
						order.getOrderItems().add(orderItem);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
					
				}
				
			}
		}
		//封装完成
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
		
	}
}