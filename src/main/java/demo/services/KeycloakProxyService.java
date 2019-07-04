package demo.services;

import java.util.List;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakProxyService {

	List<RoleRepresentation> getRoles(String userId);

	void addRoles(List<String> roles, String userId);

	void clearRoles(String userId);

	void setUserEnabled(boolean flag, String userId);

	boolean isUserEnabled(String userId);

	UserRepresentation getUserByUsername(String username);

	void createUser(UserRepresentation user);

	void updateUser(UserRepresentation user);

	GroupRepresentation getGroupById(String groupId);

	List<GroupRepresentation> getGroups();

	List<GroupRepresentation> getUserGroups(String userId);

	List<UserRepresentation> getGroupMembers(String groupId);

	void leaveGroup(String userId, String groupId);

	void joinGroup(String userId, String groupId);

	void resetUserPassword(String userId, String password);

	void resetPassword(String username, String password);

	UserRepresentation getUser(String userId);

	List<RoleRepresentation> getRealmChildRoles(String roleName);

	List<RoleRepresentation> getAssignedRoles(String userId);

	List<RoleRepresentation> getUserRoles(String userId);
}