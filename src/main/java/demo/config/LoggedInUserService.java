package demo.config;

import java.io.Serializable;

public interface LoggedInUserService extends Serializable{

	String getUsername();

	void setUsername(String username);

	String getToken();

	void setToken(String token);

}