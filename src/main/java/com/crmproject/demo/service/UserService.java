package com.crmproject.demo.service;

import com.crmproject.demo.model.User;
import com.crmproject.demo.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User cadastrar(User user) {
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        return repository.save(user);
    }

}
