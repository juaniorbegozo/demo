package demo.services;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakProxyService {
	UserRepresentation getUser(String userId);
	List<String> getRoles(String userId);
	List<String> getAllUsers();
}