// src/main/java/org/tradesphere/user/PublicUserResponseDTO.java
package org.tradesphere.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicUserResponseDTO {

    private String id;
    private String fullName;
    private String businessName;
    private String role;

    public PublicUserResponseDTO(String id, String fullName, String businessName, User.Role role) {
        this.id = id;
        this.fullName = sanitize(fullName);
        this.businessName = sanitize(businessName);
        this.role = role != null ? role.name() : null;
    }

    private String sanitize(String value) {
        if (value != null && value.contains("@")) {
            return value.substring(0, value.indexOf('@'));
        }
        return value;
    }

    public void setFullName(String fullName) {
        this.fullName = sanitize(fullName);
    }

    public void setBusinessName(String businessName) {
        this.businessName = sanitize(businessName);
    }
}