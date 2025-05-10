package com.Server.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.Server.entity.*;
import java.util.List;

public interface OtpRepository extends MongoRepository<Otp, String> {
    List<Otp> findByCode(String code);
}
