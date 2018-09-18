package service;

import org.springframework.dao.DataAccessException;

public interface UserActiveService extends UserService {

    boolean activeAccount(String email, String activecode) throws UserNotFoundException,PasswordException,DataAccessException;
}
