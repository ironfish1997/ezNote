package service.impl;

import org.apache.ibatis.executor.ExecutorException;
import org.springframework.dao.DataAccessException;

import entity.User;
import service.PasswordException;
import service.UserActiveService;
import service.UserNameException;
import service.UserNotFoundException;

public abstract class UserActiveAdapter implements UserActiveService {

	@Override
	public User Login(String name, String password)
			throws UserNotFoundException, PasswordException, DataAccessException {
		throw new ExecutorException("This method is not available");
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
