package de.sk.jetty;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;

@Path("/test")
public class Resource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/process")
	public void doHandshake(InputStream is, @Suspended final  AsyncResponse asyncResponse) {
		try {
			Response response = Response.status(HttpStatus.OK_200).entity("test").build();
			asyncResponse.resume(response);
		
		} finally {
			closeInputStream(is);
		}
	}
	
	private void closeInputStream(InputStream is) {
		if ( is != null ) {
			try {
				is.close();
			} catch(IOException e) {
				// ignore
			}
		}
	}
}
