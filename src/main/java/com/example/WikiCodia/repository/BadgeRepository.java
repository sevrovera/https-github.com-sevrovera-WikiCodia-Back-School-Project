package com.example.WikiCodia.repository;

import org.springframework.stereotype.Repository;

import com.example.WikiCodia.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BadgeRepository extends JpaRepository<Role , Long>{

}
