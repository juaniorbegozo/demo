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
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import ar.gob.mendoza.seguridad.util.log.LogLevelEnum;
import ar.gob.mendoza.seguridad.util.log.MethodLog;
import ar.gob.mendoza.seguridad.util.properties.Property;

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

	@Inject
	private RoleService roleService;

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public List<RoleRepresentation> getRoles(final String userId) {

		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			List<RoleRepresentation> rolesRepresentationList = realmResource.users()
					.get(userRepresentationList.get(0).getId()).roles().realmLevel().listEffective();
			return filterRoles(rolesRepresentationList);
		}

		return new ArrayList<>();
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public UserRepresentation getUser(final String userId) {

		UserRepresentation userRepresentation = realmResource.users().get(userId).toRepresentation();

		return userRepresentation;
	}

	private List<RoleRepresentation> filterRoles(List<RoleRepresentation> rolesRepresentationList) {
		List<RoleRepresentation> rolesToReturn = new ArrayList<>();
		List<String> roleList = this.roleService.getAllowedRoleList();
		for (RoleRepresentation roleRepresentation : rolesRepresentationList) {
			for (String role : roleList) {
				if (roleRepresentation.getName().equals(role)) {
					rolesToReturn.add(roleRepresentation);
					break;
				}
			}
		}
		return rolesToReturn;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public void clearRoles(final String userId) {
		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			UserResource userResource = realmResource.users().get(userRepresentationList.get(0).getId());
			List<RoleRepresentation> rolesRepresentationList = userResource.roles().realmLevel().listAll();

			userResource.roles().realmLevel().remove(filterRoles(rolesRepresentationList));
		}
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public void setUserEnabled(boolean flag, final String userId) {
		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			UserRepresentation userRepresentation = userRepresentationList.get(0);
			userRepresentation.setEnabled(flag);
			realmResource.users().get(userRepresentation.getId()).update(userRepresentation);
		}
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public boolean isUserEnabled(String userId) {
		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			UserRepresentation userRepresentation = userRepresentationList.get(0);
			return userRepresentation.isEnabled();
		}
		return false;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public void addRoles(final List<String> roles, final String userId) {

		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {

			UserResource userResource = realmResource.users().get(userRepresentationList.get(0).getId());

			List<RoleRepresentation> roleRepresentationList = new ArrayList<>();
			roles.forEach(role -> {
				RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
				roleRepresentationList.add(roleRepresentation);
			});

			userResource.roles().realmLevel().add(filterRoles(roleRepresentationList));

		}
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public void resetPassword(final String userId, final String password) {

		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {

			CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(password);
			credential.setTemporary(true);

			UserResource userResource = realmResource.users().get(userRepresentationList.get(0).getId());
			userResource.resetPassword(credential);

		}

	}

	@PostConstruct
	public void postConstruct() {
		Keycloak keycloak = Keycloak.getInstance(keycloakUrl, keycloakRealm, keycloakUsername, keycloakPassword,
				keycloakClientId);
		realmResource = keycloak.realm("ministerioseguridad");
	}

	@Override
	@MethodLog(level = LogLevelEnum.DEBUG)
	public UserRepresentation getUserByUsername(final String username) {
		List<UserRepresentation> userRepresentationList = realmResource.users().search(username, null, null, null, null,
				null);
		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			return userRepresentationList.stream().filter(user -> user.getUsername().equalsIgnoreCase(username))
					.findFirst().orElse(null);
		}
		return null;
	}

	@Override
	public void createUser(UserRepresentation user) {
		realmResource.users().create(user);
	}

	@Override
	public void updateUser(UserRepresentation user) {
		realmResource.users().get(user.getId()).update(user);
	}

	@Override
	public List<GroupRepresentation> getUserGroups(String userId) {
		return realmResource.users().get(userId).groups();
	}

	@Override
	public List<UserRepresentation> getGroupMembers(String groupId) {
		return realmResource.groups().group(groupId).members(0, 1000);
	}

	@Override
	public GroupRepresentation getGroupById(String groupId) {
		return realmResource.groups().group(groupId).toRepresentation();
	}

	@Override
	public List<GroupRepresentation> getGroups() {
		return realmResource.groups().groups();
	}

	@Override
	public void leaveGroup(String userId, String groupId) {
		realmResource.users().get(userId).leaveGroup(groupId);
	}

	@Override
	public void joinGroup(final String userId, final String groupId) {
		realmResource.users().get(userId).joinGroup(groupId);
	}

	@Override
	public void resetUserPassword(final String userId, final String password) {
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setValue(password);
		credential.setType(CredentialRepresentation.PASSWORD);
		realmResource.users().get(userId).resetPassword(credential);
	}

	private List<UserRepresentation> findKeycloakUser(final String userId) {
		List<UserRepresentation> userRepresentationList = realmResource.users().search(userId, null, null, null, null,
				null);
		userRepresentationList = userRepresentationList.stream()
				.filter(elem -> elem.getUsername().equalsIgnoreCase(userId)).collect(Collectors.toList());
		return userRepresentationList;
	}

	@Transactional(value = TxType.REQUIRED)
	@Override
	public List<RoleRepresentation> getRealmChildRoles(String roleName) {
		return new ArrayList<>(realmResource.roles().get(roleName).getRoleComposites());
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public List<RoleRepresentation> getAssignedRoles(final String userId) {

		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {

			return realmResource.users().get(userRepresentationList.get(0).getId()).roles().realmLevel().listAll();

		}

		return new ArrayList<>();
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@MethodLog(level = LogLevelEnum.DEBUG)
	public List<RoleRepresentation> getUserRoles(final String userId) {

		List<UserRepresentation> userRepresentationList = findKeycloakUser(userId);

		if (CollectionUtils.isNotEmpty(userRepresentationList)) {
			List<RoleRepresentation> rolesRepresentationList = realmResource.users()
					.get(userRepresentationList.get(0).getId()).roles().realmLevel().listEffective();
			return rolesRepresentationList;
		}

		
		return new ArrayList<>();
	}
}