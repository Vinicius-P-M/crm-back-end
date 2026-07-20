package com.crmproject.demo.dto;

import com.crmproject.demo.model.Role;

public record UserResponse(String email, Role role) {
}
