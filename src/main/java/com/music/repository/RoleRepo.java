package com.music.repository;

import com.music.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepo extends MongoRepository<Role, String> {
}
