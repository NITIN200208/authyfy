package com.example.project.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.project.Repository.UserRepository;
import com.example.project.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String email) {
	    UserEntity user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

	    return new org.springframework.security.core.userdetails.User(
	            user.getEmail(), user.getPassword(), new ArrayList<>());
	}

	

}
