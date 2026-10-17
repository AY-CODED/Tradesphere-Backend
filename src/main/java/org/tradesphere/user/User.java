// src/main/java/org/tradesphere/user/User.java
package org.tradesphere.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String phoneNumber;

    private String password;

    private Role role; // BUYER, VENDOR, ADMIN

    public enum Role {
        BUYER, VENDOR, ADMIN
    }
}