package com.li.bbs.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private Long id;

    private String name; // e.g. ROLE_USER, ROLE_MOD, ROLE_ADMIN

    private String description;

}

