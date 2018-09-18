package dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import entity.User;

@Repository("userDao")
public interface UserDao {
    User findUserByEmail(String email) throws DataAccessException;

    int addUser(User user) throws DataAccessException;

    User findUserById(String userId) throws DataAccessException;

    int updateUser(@Param("user") User user) throws DataAccessException;
}
