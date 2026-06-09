package com.li.bbs.repository;

import com.li.bbs.domain.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleRepository {
    Role findByName(@org.apache.ibatis.annotations.Param("name") String name);

    int insert(Role role);
}

