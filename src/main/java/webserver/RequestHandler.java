package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;
import util.HttpSession;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	HttpRequest req = new HttpRequest(in);
        	HttpResponse res = new HttpResponse(out);
        	String url = req.getUrl();
        	
        	if(Strings.isNullOrEmpty(req.getCookie("JSESSIONID"))) {
        		res.addHeader("Set-Cookie", "JSESSIONID="+UUID.randomUUID());
        	}
        	
        	Controller controller = ControllerMapping.getController(req.getUrl());
        	if (controller == null) {
        		res.forward("./webapp/"+url);
        	} else {
        		controller.service(req, res);
        	} 	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
