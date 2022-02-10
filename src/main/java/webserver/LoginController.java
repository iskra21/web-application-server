package webserver;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

public class LoginController implements Controller {

	@Override
	public void service(HttpRequest req, HttpResponse res) {
		if (canLogin(req)) {
			res.addHeader("Set-Cookie", "logined=true");
			res.sendRedirect("/index.html");
		} else {
			res.addHeader("Set-Cookie", "logined=false");
			res.sendRedirect("/user/login_failed.html");
		}
	}
	
	private boolean canLogin(HttpRequest req) {
    	User user = DataBase.findUserById(req.getParameter("userId"));
    	if ((user != null) && user.getPassword().equals(req.getParameter("password"))) {
    		return true;
    	}
    	return false;
	}
}
