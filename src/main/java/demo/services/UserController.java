package demo.services;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import demo.security.Secure;

@Stateless
@Path("/user")
public class UserController {

	@Inject
	private KeycloakProxyService keycloakService;	
	

	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	@Secure(allowedRoles = { demo.security.AllowedRolEnum.ADMIN, demo.security.AllowedRolEnum.USER })
	public Response getUserInfo() throws IllegalAccessException, InvocationTargetException {

		return Response.status(200).entity("Hello from Sigsense!").build();
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Secure(allowedRoles = { demo.security.AllowedRolEnum.ADMIN })
	public Response getAllUsers() throws IllegalAccessException, InvocationTargetException {
		
		return Response.status(200).entity(keycloakService.getAllUsers()).build();
	}
	 
}
