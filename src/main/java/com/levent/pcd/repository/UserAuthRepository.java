package com.levent.pcd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.levent.pcd.model.UserAuth;

@Repository("userAuthRepository")
public interface UserAuthRepository extends MongoRepository<UserAuth,String> {	
	
	UserAuth findByUsernameAndPassword(String username, String password);
	UserAuth findByUsername(String username);
	
}
