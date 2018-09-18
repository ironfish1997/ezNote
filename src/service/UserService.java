package service;

import org.springframework.dao.DataAccessException;
import org.springframework.web.multipart.MultipartFile;

import entity.User;

/**
 * 业务层接口
 */
public interface UserService {
    /**
     * 登录功能，登录成功返回登录信息，失败则抛出异常
     * @param email
     * @param password
     * @return 登录成功就返回登录用户信息
     * @throws UserNotFoundException 用户不存在
     * @throws PasswordException 密码错误
     */
    User Login(String email, String password) throws UserNotFoundException,PasswordException,DataAccessException;
    User Register(String email,String password,String nick,String confirm) throws UserNameException,PasswordException,DataAccessException;
    boolean Existed(String email) throws DataAccessException;
    boolean Update(String email,String nickName, String originPassword,String password,String confirm) throws UserNotFoundException,PasswordException,DataAccessException;
    boolean UpdateUserAvatar(String email, MultipartFile avatarUrl) throws UserNotFoundException,PasswordException,DataAccessException;
}