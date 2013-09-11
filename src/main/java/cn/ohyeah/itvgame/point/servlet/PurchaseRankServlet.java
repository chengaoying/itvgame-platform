package cn.ohyeah.itvgame.point.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import cn.ohyeah.itvgame.global.BeanManager;
import cn.ohyeah.itvgame.platform.service.PurchaseRecordService;
import cn.ohyeah.itvgame.platform.viewmodel.PurchaseStatis;

public class PurchaseRankServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8309844411746031688L;
	
	private static final PurchaseRecordService purchaseService;
	
	static{
		purchaseService = (PurchaseRecordService)BeanManager.getBean("purchaseRecordService");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Object p = req.getParameter("productId");
		
		int productId = -1;
		int offset = Integer.parseInt(String.valueOf(req.getParameter("offset")));
		int lenght = Integer.parseInt(String.valueOf(req.getParameter("lenght")));;
		String sTime = String.valueOf(req.getParameter("sTime"));
		String eTime = String.valueOf(req.getParameter("eTime"));
		if(p==null){
			productId = 19;
		}else{
			productId = Integer.parseInt(String.valueOf(p));
		}
		
		List<PurchaseStatis> list = purchaseService.queryPurchaseStatis(productId, offset, lenght, sTime, eTime);
		resp.setContentType("text/html;charset=UTF-8");
		//resp.setContentType("application/x-javascript;charset=UTF-8");
		
		JSONArray json = JSONArray.fromObject(list); 
		System.out.println(json);
		resp.getWriter().write(json.toString()); 
		resp.getWriter().flush();
		resp.getWriter().close();
		
	}

}
