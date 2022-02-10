package webserver;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Strings;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

public class ListUserController implements Controller {

	@Override
	public void service(HttpRequest req, HttpResponse res) {
		if (isLogin(req)) {
			StringBuilder body = new StringBuilder();
			Collection<User> users = DataBase.findAll();
			body.append("<table border='1'>");
			body.append("<tr><td>UserId</td><td>Password</td><td>Name</td><td>E-Mail</td></tr>");
			for (User user:users) {
				body.append("<tr>");
				body.append("<td>"+user.getUserId()+"</td>");
				body.append("<td>"+user.getPassword()+"</td>");
				body.append("<td>"+user.getName()+"</td>");
				body.append("<td>"+user.getEmail()+"</td>");
				body.append("</tr>");
			}
			body.append("</table>");
			res.forwardBody(body.toString());
		} else {
			res.sendRedirect("/index.html");
		}
	}
	
    private boolean isLogin(HttpRequest req) {
    	String value = req.getHeader("Cookie");
    	if (Strings.isNullOrEmpty(value)) {
    		return false;
    	}
    	Map<String,String> cookie = util.HttpRequestUtils.parseCookies(value);
    	String isLogin = cookie.get("logined");
    	if (!Strings.isNullOrEmpty(isLogin) && Boolean.parseBoolean(isLogin)) {
    		return true;
    	}
    	return false;
	}

}
