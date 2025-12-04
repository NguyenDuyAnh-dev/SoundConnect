package com.example.demo.dto.request;

import com.example.demo.validator.DobConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
     @Pattern(
             regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
             message ="INVALID_PASSWORD"
     )
     String password;
     String firstname;
     String lastname;
     String hometown;
     String gender;
     Boolean available;

     @DobConstraint(min = 18, message = "INVALID_DOB")
     @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate dob;
     List<String> roles;

}
