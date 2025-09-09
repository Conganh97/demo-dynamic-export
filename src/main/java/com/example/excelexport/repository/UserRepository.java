package com.example.excelexport.repository;

import com.example.excelexport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(String status);
    List<User> findByDepartment(String department);
}

