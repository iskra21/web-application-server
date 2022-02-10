package webserver;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

public class CreateUserController implements Controller {

	@Override
	public void service(HttpRequest req, HttpResponse res) {
		User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"), req.getParameter("email"));
		DataBase.addUser(user);
		res.sendRedirect("/index.html");
	}
}
