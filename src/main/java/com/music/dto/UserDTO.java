package com.music.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String login;
    private String password;
    private Boolean active;
    private Set<RoleDTO> roles;
    private String token;  // Used for login/register responses
}
