// src/main/java/org/tradesphere/user/BusinessVerificationRepository.java
package org.tradesphere.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BusinessVerificationRepository extends MongoRepository<BusinessVerificationDoc, String> {
    Optional<BusinessVerificationDoc> findByUserId(String userId);
}