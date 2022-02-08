package util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HttpRequestTest {
	private String testDir = ".\\src\\test\\resources\\";
	
	@Test
	public void request_GET() throws IOException {
		InputStream in = new FileInputStream(new File(testDir + "Http_GET.txt"));
		HttpRequest request= new HttpRequest(in);
		
		assertEquals("GET", request.getMethod());
		assertEquals("/user/create", request.getUrl());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userId"));
	}
	
	@Test
	public void request_POST() throws IOException {
		InputStream in = new FileInputStream(new File(testDir + "Http_POST.txt"));
		HttpRequest request= new HttpRequest(in);
		
		assertEquals("POST", request.getMethod());
		assertEquals("/user/create", request.getUrl());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userId"));
	}
}
