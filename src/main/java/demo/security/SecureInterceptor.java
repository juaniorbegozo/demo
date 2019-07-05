package demo.security;

import java.lang.annotation.Annotation;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

import demo.services.KeycloakProxyService;

@Secure
@Interceptor
public class SecureInterceptor {
	private static final String SIN_AUTORIZACION = "No está autorizado para realizar esta acción";
	private static final String ACCESO = "Acceso";

	@Inject
	private HttpServletRequest request;
	
	@Inject
	private KeycloakProxyService keycloakService;

	@AroundInvoke
	public Object validateRoles(InvocationContext ctx) throws Exception {

		SecurityContext securityContext = ResteasyProviderFactory.getContextData(SecurityContext.class);

		AllowedRolEnum[] rolesAllowed = null;
		for (Annotation annotation : ctx.getMethod().getDeclaredAnnotations()) {
			if (annotation instanceof Secure) {
				rolesAllowed = ((Secure) annotation).allowedRoles();
			}
		}

		Principal principal = request.getUserPrincipal();

		if (principal == null) {
			return Response.status(403).build();
		}
		
		KeycloakSecurityContext keycloakSecurityContext = ((KeycloakPrincipal) securityContext.getUserPrincipal())
				.getKeycloakSecurityContext();

		List<String> userRoles = keycloakService.getRoles(keycloakSecurityContext.getToken().getPreferredUsername());
		if (Arrays.asList(rolesAllowed).stream().anyMatch(role -> userRoles.contains(role))) {
			return ctx.proceed();
		} else {
			Map<String, String> message = new HashMap<>();
			message.put(ACCESO, SIN_AUTORIZACION);
			return Response.status(401).entity(message).build();
		}

	}


}
