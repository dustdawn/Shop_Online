package com.dustdawn.web.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.dustdawn.entity.Category;
import com.dustdawn.entity.Product;
import com.dustdawn.service.AdminService;
import com.dustdawn.utils.CommonsUtils;



public class AdminAddProductServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//目的：搜集表单数据并封装成实体Product  将图片存到服务器磁盘
		
		Product product = new Product();
		Map<String,Object> map = new HashMap<String,Object>();
		try {
		//创建磁盘文件项
			DiskFileItemFactory factory = new DiskFileItemFactory();
			//创建文件上传的核心对象
			ServletFileUpload upload = new ServletFileUpload(factory);
			//解析request获得文件项对象集合
			
			List<FileItem> parseRequest = upload.parseRequest(request);
			for(FileItem item : parseRequest) {
				boolean formField = item.isFormField();
				if(formField) {
					//普通表单项 获得表单的数据，封装到Product实体中
					String fieldName = item.getFieldName();
					String fileValue = item.getString("UTF-8");
					
					map.put(fieldName, fileValue);
				}else {
					//文件上传项 获得文件名称  获得文件内容
					String filename = item.getName();
					String path = this.getServletContext().getRealPath("upload");
					InputStream in = item.getInputStream();
					OutputStream out = new FileOutputStream(path+"/"+filename);
					//不写filename拒绝访问
					IOUtils.copy(in, out);
					in.close();
					out.close();
					item.delete();
					
					map.put("pimage", "upload/"+filename);
				}
				
				
			}
			
			BeanUtils.populate(product, map);
			//pid pimage pdata pflag  category
			product.setPid(CommonsUtils.getUUID());
			product.setPdate(new Date());
			product.setPflag(0);
			Category category = new Category();
			category.setCid(map.get("cid").toString());
			product.setCategory(category);
			
			//将封装好的product传递给service层
			AdminService service = new AdminService();
			service.saveProduct(product);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect(request.getContextPath()+"/admin?method=findAllProduct");
		
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}