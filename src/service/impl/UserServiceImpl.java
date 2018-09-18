package service.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dao.UserDao;
import entity.User;
import service.PasswordException;
import service.UserNameException;
import service.UserNotFoundException;
import service.UserService;
import util.MailUtil;
import util.ServerFileAccessUtil;

@Service("userService")
public class UserServiceImpl implements UserService {

    public UserServiceImpl() {

    }

    @Resource
    UserDao userDao;

    @Value("#{db.salt}")
    private String salt;

    @Override
    public User Login(String email, String password) throws UserNotFoundException, PasswordException {
        if (email == null || email.trim().isEmpty()) {
            throw new UserNotFoundException("邮箱为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordException("密码为空");
        }
        password = DigestUtils.md5Hex(salt + password.trim());
        User user=null;
        try{
        	user = userDao.findUserByEmail(email.trim());
        }catch(RuntimeException e){
        	e.printStackTrace();
        	throw new UserNotFoundException("服务器错误");
        }
        if (user == null) {
            throw new UserNotFoundException("此用户不存在");
        } else if (!user.getPassword().equals(password.trim())) {
            throw new PasswordException("密码错误");
        }else if((user.getActived()!=1)){
        	throw new UserNotFoundException("用户未激活,请检查您的邮箱是否接收到激活邮件");
        }
        return user;
    }

    @Override
    public User Register(String email, String password, String nick, String confirm) throws UserNameException, PasswordException {
        //检查name，不能和数据库中的重复
        if (email == null || email.trim().isEmpty()) {
            throw new UserNameException("邮箱不能为空");
        }
        User check = userDao.findUserByEmail(email);
        if (check != null) {
            throw new UserNameException("用户名已被注册");
        }
        //检查密码
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordException("密码不能为空");
        }
        if (!password.equals(confirm)) {
            throw new PasswordException("两次密码输入不一样");
        }
        //检查nick
        if (nick == null || nick.trim().isEmpty()) {
            nick = email;
        }

        String id = UUID.randomUUID().toString();
        String token = null;
        String password2 = DigestUtils.md5Hex(salt + password.trim());
        User user = new User(id, email, password2, token, nick,0,UUID.randomUUID().toString());
        int n = userDao.addUser(user);
        if (n != 1) {
            throw new RuntimeException("添加失败");
        }else{
        	try {
				MailUtil.sendActiveMail(email, user.getActive_code());
			} catch (InterruptedException | IOException | TimeoutException | ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException("激活邮件发送失败");
			}
        }
        return user;
    }

    @Override
    public boolean Update(String email,String nickName, String originPassword, String password, String confirm) throws UserNotFoundException, PasswordException {
        //检查用户名，密码，确认密码是否格式正确
        if (email == null || email.trim().isEmpty()) {
            throw new UserNotFoundException("邮箱为空");
        }
        if(nickName==null||nickName.trim().isEmpty()){
        	throw new UserNotFoundException("用户昵称为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordException("密码为空");
        }
        String passwordConf = confirm.trim();
        if (confirm == null || !(passwordConf.equals(password.trim()))) {
            throw new PasswordException("确认密码不一致");
        }
        User user = Login(email, originPassword);
        //数据库里没有这个账号就报错
        if (user == null) {
            throw new UserNotFoundException("账号不存在");
        }
        String password2 = DigestUtils.md5Hex(salt + password.trim());
        user.setPassword(password2);
        user.setNick(nickName);
        int n = userDao.updateUser(user);
        return n == 1;
    }

	@Override
	public boolean Existed(String email) throws UserNotFoundException, DataAccessException {
		//检查用户名，密码，确认密码是否格式正确
        if (email == null || email.trim().isEmpty()) 
            throw new UserNotFoundException("邮箱为空");
        
        User user=null;
        try{
        	user = userDao.findUserByEmail(email.trim());
        }catch(RuntimeException e){
        	e.printStackTrace();
        	throw new UserNotFoundException("服务器错误");
        }
        return user != null;
	}

	@Override
	public boolean UpdateUserAvatar(String email, MultipartFile avatar)
			throws UserNotFoundException, PasswordException, DataAccessException {
		//更新头像
        if (email == null || email.trim().isEmpty()) 
            throw new UserNotFoundException("邮箱为空");
		User user = userDao.findUserByEmail(email.trim());
		ServerFileAccessUtil.uploadAvatar(user.getId(), avatar);
		return ServerFileAccessUtil.isAvatarExisted(user.getId());
	}
	
}
