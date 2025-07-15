package com.example.project.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.io.AuthRequest;
import com.example.project.io.AuthResponse;
import com.example.project.io.ResetPasswordRequest;
import com.example.project.service.AppUserDetailsService;
import com.example.project.service.ProfileService;
import com.example.project.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    
    private final ProfileService profileService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());

            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());

            final String jwtToken = jwtUtil.generateToken(userDetails);

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .secure(false)
                    .sameSite("Strict")
                    .build();

            AuthResponse authResponse = new AuthResponse(request.getEmail(), jwtToken, true, "Login successful");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(authResponse);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, false, "Email or password is incorrect"));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, false, "Account is disabled"));
        } catch (Exception ex) {
            ex.printStackTrace(); // ✅ Debug this!
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, false, "Authentication failed"));
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
    
    
    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication?.name") String email){
    	 return ResponseEntity.ok(email!=null);
    }
    
    
    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
    	try {
    		profileService.sendResetOtp(email);
    	}
    	catch(Exception e) {
    		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
    	}
    	
    }
    
    @PostMapping("/reset-passwprd")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    	try {
    		profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
    	} catch(Exception e) {
    		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
    	}
    }
    
    
    @PostMapping("/send-otp")
    public void resetPassword(@CurrentSecurityContext(expression = "authentication?.name")String email) {
    	try {
    		profileService.sendOtp(email);
    	} catch(Exception e) {
    		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
    	}
    }
    
    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String,Object> request,
    		@CurrentSecurityContext(expression = "authentication?.name")String email) {
    	
    	if(request.get("otp").toString()==null) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing details");
    	}
    	
    	try {
    		profileService.verityOtp(email, request.get("otp").toString());
    	}
    	catch(Exception ex) {
    		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage());
    	}
    }
}
