package webserver;

import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstractController implements Controller {

	
	@Override
	public void service(HttpRequest req, HttpResponse res) {
		// TODO Auto-generated method stub

	}
	
	public abstract void doPost(HttpRequest req, HttpResponse res);
	public abstract void doGet(HttpRequest req, HttpResponse res);

}
