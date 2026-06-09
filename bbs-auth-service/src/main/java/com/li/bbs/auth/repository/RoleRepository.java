package com.li.bbs.auth.repository;

import com.li.bbs.auth.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleRepository {
    Role findByName(@Param("name") String name);

    int insert(Role role);
}
