package com.example.project.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {
	
	@NotBlank(message="name shuold be not empty")
    private String name;
	
	@Email(message="Enter valid Email address")
	@NotNull(message="Email should be not empty")
    private String email;
    
	@Size(min=6,message="passsword must be atleast 6 characters")
    private String password;
}
