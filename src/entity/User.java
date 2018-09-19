package entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component("user")
public class User implements Serializable {
    private static final long serialVersionUID = -8228569662628197300L;
    private String id = null;
    private String email = null;
    private String password = null;
    private String token = null;
    private String nick = null;
    private Integer actived=-1;
    private String active_code=null;
    private Date expireTime=null;

    public User(String id, String email, String password, String token, String nick,Integer actived,String active_code,Date expireTime) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.token = token;
        this.nick = nick;
        this.actived=actived;
        this.active_code=active_code;
        this.expireTime=expireTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", email='" + email + '\'' + ", password='" + password + '\'' + ", token='" + token + '\'' + ", nick='" + nick + '\'' + '}';
    }

	public int getActived() {
		return actived;
	}

	public void setActived(int actived) {
		this.actived = actived;
	}

	public String getActive_code() {
		return active_code;
	}

	public void setActive_code(String active_code) {
		this.active_code = active_code;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
}
