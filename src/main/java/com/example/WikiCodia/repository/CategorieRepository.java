package com.example.WikiCodia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WikiCodia.model.Categorie;


public interface CategorieRepository extends JpaRepository<Categorie, Long> {
	List<Categorie> findByLibCategorieContaining(String libCategorie);
}
