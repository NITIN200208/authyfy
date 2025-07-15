package com.example.project.service;

import com.example.project.Repository.UserRepository;
import com.example.project.entity.UserEntity;
import com.example.project.io.ProfileRequest;
import com.example.project.io.ProfileResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final EmailService emailService;
    
    

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        // ✅ 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // ✅ 2. Convert request to UserEntity
        UserEntity newProfile = convertToUserEntity(request);

        // ✅ 3. Save the new user
        newProfile = userRepository.save(newProfile);

        // ✅ 4. Convert saved entity to response
        return convertToProfileResponse(newProfile);
    }
    
    
    
    @Override
  	public ProfileResponse getProfile(String email) {
  		// TODO Auto-generated method stub
    UserEntity existingUser=userRepository.findByEmail(email).orElseThrow(()->
    	new UsernameNotFoundException("User not found :" +email));
  		return convertToProfileResponse(existingUser);
  	}
    
    

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerify(false)
                .build();
    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerify(newProfile.getIsAccountVerify())
                .build();
    }



	@Override
	public void sendResetOtp(String email) {
		// TODO Auto-generated method stub
		UserEntity existingEntity=userRepository.findByEmail(email)
				.orElseThrow(()->new UsernameNotFoundException("User not found" +email));
		
		String otp=String.valueOf(ThreadLocalRandom.current().nextInt(10000,100000));
		
		//calculate expire time current time + 24 hour in milisecond
		
		long expireTime=System.currentTimeMillis()+(15*60*1000);
		
		//update the profile/user
		
		existingEntity.setResetOtp(otp);
		existingEntity.setResetOtpExpire(expireTime);
		
		//save into the database
		
		userRepository.save(existingEntity);
		
		
		try {
			emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
		}
		catch(Exception ex) {
			throw new RuntimeException("unable to send email");
		}
		
	}

	@Override
	public void resetPassword(String email, String Otp, String newPassword) {
	    UserEntity existingUser = userRepository.findByEmail(email)
	        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

	    // ✅ Check if OTP is null or doesn't match
	    if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(Otp)) {
	        throw new RuntimeException("Invalid OTP");
	    }

	    // ✅ Check if OTP is expired
	    if (existingUser.getResetOtpExpire() < System.currentTimeMillis()) {
	        throw new RuntimeException("OTP has expired");
	    }

	    // ✅ Set new password and clear OTP + expiry
	    existingUser.setPassword(passwordEncoder.encode(newPassword));
	    existingUser.setResetOtp(null);              
	    existingUser.setResetOtpExpire(0L);       

	    userRepository.save(existingUser);
	}

	

	@Override
	public void sendOtp(String email) {
		// TODO Auto-generated method stub
		UserEntity existingUser=userRepository.findByEmail(email)
				.orElseThrow(()->new UsernameNotFoundException("user not found:" +email));
		
		if(existingUser.getIsAccountVerify()!=null  && existingUser.getIsAccountVerify()) {
			return;
			}
			
		//generate 6 digit
		
		String otp=String.valueOf(ThreadLocalRandom.current().nextInt(10000,100000));
		
		//calculate expire time current time + 24 hour in milisecond
		
		long expireTime=System.currentTimeMillis() + (24*60*60*1000);
		
		//update the user  entity
		
		existingUser.setVerifyOtp(otp);
		existingUser.setVerifyOtpExpireAt(expireTime);
			
		// save to databases
		userRepository.save(existingUser);
		
		try {
			emailService.sendResetOtpEmail(existingUser.getEmail(), otp);
		}
		catch(Exception ex) {
			throw new RuntimeException("unable to send email");
		}
		
	}



	@Override
	public void verityOtp(String email, String otp) {
		// TODO Auto-generated method stub
		UserEntity existingUser=userRepository.findByEmail(email)
				.orElseThrow(()->new UsernameNotFoundException("User not found " +email));
		
		if(existingUser.getVerifyOtp()==null  || !existingUser.getVerifyOtp().equals(otp)) {
			throw new RuntimeException("Invalid OTP");
		}
		
		if(existingUser.getVerifyOtpExpireAt()<System.currentTimeMillis()) {
			throw new RuntimeException("OTP Expire");
			
		}
				
		existingUser.setIsAccountVerify(true);
		existingUser.setVerifyOtp(null);
		existingUser.setVerifyOtpExpireAt(0L);
		
		userRepository.save(existingUser);
		
	}



	@Override
	public String getLoggedInUserId(String email) {
		// TODO Auto-generated method stub
	UserEntity existingUser=userRepository.findByEmail(email)
		.orElseThrow(()->new UsernameNotFoundException("user not found:" +email));
		return existingUser.getUserId();
	}




	
  
}

	

