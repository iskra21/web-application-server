package webserver;

import util.HttpRequest;
import util.HttpResponse;

public interface Controller {
	public void service(HttpRequest req, HttpResponse res);
}
