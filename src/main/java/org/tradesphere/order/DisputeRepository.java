// src/main/java/org/tradesphere/order/DisputeRepository.java
package org.tradesphere.order;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DisputeRepository extends MongoRepository<Dispute, String> {}