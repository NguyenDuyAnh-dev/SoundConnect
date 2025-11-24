package com.example.demo.dto.request;

import lombok.Data;

@Data
public class UserDTO {
        private String id;
        private String username;
        private String avatar; // Giả sử avatar là một URL hoặc Base64 string
    }

