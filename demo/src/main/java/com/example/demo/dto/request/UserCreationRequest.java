package com.example.demo.dto.request;

import com.example.demo.validator.DobConstraint;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 3, message ="INVALID_USERNAME")
     String username;

    @Size(min = 8, message ="INVALID_PASSWORD")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
            message ="INVALID_PASSWORD"
    )
     String password;
    
     String firstname;
     String lastname;
     @DobConstraint(min = 18, message = "INVALID_DOB")
     LocalDate dob;

}