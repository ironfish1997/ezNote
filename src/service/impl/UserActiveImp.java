package service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dao.UserDao;
import entity.User;
import service.PasswordException;
import service.UserNotFoundException;

@Service("userActiveImp")
public class UserActiveImp extends UserActiveAdapter {

	@Autowired
	private UserDao userDao;
	
	@Override
	public boolean activeAccount(String email, String activecode)
			throws UserNotFoundException, PasswordException, DataAccessException {
		 //检查用户名，密码，确认密码是否格式正确
        if (email == null || email.trim().isEmpty()) {
            throw new UserNotFoundException("用户邮箱为空");
        }
        if(activecode==null||activecode.trim().isEmpty()){
        	throw new UserNotFoundException("激活码为空");
        }
		try{
			User u = userDao.findUserByEmail(email);
			if(!u.getActive_code().trim().equals(activecode.trim())){
				throw new UserNotFoundException("激活码错误");
			}
			u.setActived(1);
			userDao.updateUser(u);
		}
		catch(Exception e){
			throw e;
		}
		return true;
	}

	@Override
	public boolean Existed(String name) throws DataAccessException {
		throw new RuntimeException("该方法不可用");
	}

	@Override
	public boolean UpdateUserAvatar(String name, MultipartFile avatarUrl)
			throws UserNotFoundException, PasswordException, DataAccessException {
		throw new RuntimeException("该方法不可用");
	}

}
