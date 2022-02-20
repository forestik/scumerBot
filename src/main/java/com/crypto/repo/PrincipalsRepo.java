package com.crypto.repo;

import com.crypto.entity.Principal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrincipalsRepo extends JpaRepository<Principal, Long> {

     Optional<Principal> findByUserName(@Param("userName") String userName);

//     Optional<Principal> findByFirstNameOrLastName(@Param("firstName")String firstName, @Param("lastName")String lastName);

}
