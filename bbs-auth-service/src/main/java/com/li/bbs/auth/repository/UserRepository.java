package com.li.bbs.auth.repository;

import com.li.bbs.auth.domain.User;
import com.li.bbs.auth.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRepository {

    User findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);

    int insert(User user);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<Role> findRolesByUserId(@Param("userId") Long userId);
}

