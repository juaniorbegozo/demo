package demo.config;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@SessionScoped
public class LoggedInUserServiceImpl implements LoggedInUserService {

	private static final long serialVersionUID = 3675887631198162645L;
	private String username = "default";
	
	private String token = "";

	@Produces
	@Named("username")
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}
		

	@Produces
	@Named("token")
	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void setToken(String token) {
		this.token = token;
	}

}
