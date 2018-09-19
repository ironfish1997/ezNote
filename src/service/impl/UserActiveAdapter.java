package service.impl;

import org.apache.ibatis.executor.ExecutorException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.multipart.MultipartFile;

import entity.User;
import service.PasswordException;
import service.UserActiveService;
import service.UserNameException;
import service.UserNotFoundException;

public abstract class UserActiveAdapter implements UserActiveService {

	@Override
	public User Login(String email, String password, String expireTime)
			throws UserNotFoundException, PasswordException, DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User checkToken(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean Existed(String email) throws DataAccessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean UpdateUserAvatar(String email, MultipartFile avatarUrl)
			throws UserNotFoundException, PasswordException, DataAccessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User Register(String name, String password, String nick, String confirm)
			throws UserNameException, PasswordException, DataAccessException {
		throw new ExecutorException("This method is not available");
	}

	@Override
	public boolean Update(String name, String nickName,String originPassword, String password, String confirm)
			throws UserNotFoundException, PasswordException, DataAccessException {
		throw new ExecutorException("This method is not available");
	}

	@Override
	public abstract boolean activeAccount(String name, String activecode)
			throws UserNotFoundException, PasswordException, DataAccessException;

}
