package demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import demo.security.Property;

@RequestScoped
public class KeycloakProxyServiceImpl implements KeycloakProxyService {

	@Property("keycloak.url")
	@Inject
	private String keycloakUrl;

	@Property("keycloak.realm")
	@Inject
	private String keycloakRealm;

	@Property("keycloak.username")
	@Inject
	private String keycloakUsername;

	@Property("keycloak.password")
	@Inject
	private String keycloakPassword;

	@Property("keycloak.clientid")
	@Inject
	private String keycloakClientId;

	private RealmResource realmResource;

	@Override
	@Transactional(value = TxType.REQUIRED)
	public UserRepresentation getUser(final String userId) {

		UserRepresentation userRepresentation = realmResource.users().get(userId).toRepresentation();

		return userRepresentation;
	}
	
	@Override
	@Transactional(value = TxType.REQUIRED)
	public List<String> getRoles(final String userId) {
		List<UserRepresentation> userRepresentationList = realmResource.users().search(userId, null, null, null, null,
				null);
		userRepresentationList = userRepresentationList.stream()
				.filter(elem -> elem.getUsername().equalsIgnoreCase(userId)).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			List<RoleRepresentation> rolesRepresentationList = realmResource.users()
					.get(userRepresentationList.get(0).getId()).roles().realmLevel().listEffective();
			return rolesRepresentationList.stream().map(role -> role.getName()).collect(Collectors.toList());
		}

		return new ArrayList<>();
	}
	
	@Override
	@Transactional(value = TxType.REQUIRED)
	public List<String> getAllUsers() {

		List<UserRepresentation> userRepresentationList = realmResource.users().search(null, null, null, null, null,
				null);

		return userRepresentationList.stream().map(u -> u.getUsername()).collect(Collectors.toList());
	}



	@PostConstruct
	public void postConstruct() {
		Keycloak keycloak = Keycloak.getInstance(keycloakUrl, keycloakRealm, keycloakUsername, keycloakPassword,
				keycloakClientId);
		realmResource = keycloak.realm("ministerioseguridad");
	}

}