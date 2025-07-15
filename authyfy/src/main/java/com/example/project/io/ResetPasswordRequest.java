package com.example.project.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

	
	@NotBlank(message="NewPassword is required")
	private String newPassword;
	
	@NotBlank(message="OTP is required")
	private String Otp;
	
	@NotBlank(message="Email is required")
	private String email;
}
