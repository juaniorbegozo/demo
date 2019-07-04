package demo.config;

import java.io.IOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

@Provider
public class UserLoggedInFilter implements ContainerRequestFilter {

	@Inject
	private Instance<LoggedInUserService> loggedInUserService;
	
	@Context 
	private SecurityContext secContext;
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(secContext.getUserPrincipal() != null){
			KeycloakSecurityContext keycloakSecurityContext = ((KeycloakPrincipal) secContext.getUserPrincipal())
					.getKeycloakSecurityContext();
			loggedInUserService.get().setUsername(keycloakSecurityContext.getToken().getPreferredUsername());
			loggedInUserService.get().setToken(keycloakSecurityContext.getTokenString());
		}
	}

}
