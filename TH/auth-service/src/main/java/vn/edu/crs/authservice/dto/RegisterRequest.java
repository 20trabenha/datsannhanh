package vn.edu.crs.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.crs.authservice.entity.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}