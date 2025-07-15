package com.example.project.service;

import org.springframework.stereotype.Service;

import com.example.project.io.ProfileRequest;
import com.example.project.io.ProfileResponse;

@Service
public interface ProfileService {
	
	ProfileResponse createProfile(ProfileRequest request);
	
	ProfileResponse getProfile(String email);
	
	 void sendResetOtp(String email);
	 
	 void resetPassword(String email,String Otp,String newPassword);
	 
	 void sendOtp(String email);
	 
	 void verityOtp(String userId,String otp);
	 
	 String getLoggedInUserId(String email);
	

}
