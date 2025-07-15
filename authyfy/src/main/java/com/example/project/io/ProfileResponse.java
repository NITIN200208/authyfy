package com.example.project.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerify;
}
