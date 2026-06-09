package com.li.bbs.user.repository;

import com.li.bbs.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    int updateProfile(@Param("id") Long id,
                      @Param("displayName") String displayName,
                      @Param("profile") String profile);

    int insert(User user);
}

