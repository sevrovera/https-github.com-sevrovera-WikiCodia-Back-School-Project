package com.example.WikiCodia.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.WikiCodia.model.Article;
import com.example.WikiCodia.model.Categorie;
import com.example.WikiCodia.model.Framework;
import com.example.WikiCodia.model.Langage;
import com.example.WikiCodia.model.Type;
import com.example.WikiCodia.model.Utilisateur;
import com.example.WikiCodia.model.Vote;
import com.example.WikiCodia.repository.ArticleRepository;
import com.example.WikiCodia.repository.CategorieRepository;
import com.example.WikiCodia.repository.EtatRepository;
import com.example.WikiCodia.repository.FrameworkRepository;
import com.example.WikiCodia.repository.GuildeRepository;
import com.example.WikiCodia.repository.LangageRepository;
import com.example.WikiCodia.repository.RoleRepository;
import com.example.WikiCodia.repository.TypeRepository;
import com.example.WikiCodia.repository.UtilisateurRepository;
//import com.example.WikiCodia.repository.VoteRepository;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
//@CrossOrigin(origins = "*", allowedHeaders = "*")

@RestController
@RequestMapping("/articles")
public class ArticleController {

	@Autowired
	ArticleRepository articleRepository;
	
	@Autowired
	GuildeRepository guildeRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	TypeRepository typeRepository;
	
	@Autowired
	UtilisateurRepository utilisateurRepository;
	
	
//	@Autowired
//	VoteRepository voteRepository;
	
	@Autowired
	LangageRepository langageRepository;
	
	
	@Autowired
	FrameworkRepository frameworkRepository;
	
	
	@Autowired
	CategorieRepository categorieRepository;
	
	
	@Autowired
	EtatRepository etatRepository;
	
	Map<Long, Article> articles;

	@GetMapping("/all")
	public ResponseEntity<List<Article>> getAllArticles(@RequestParam(required = false) String titre) {
		try {
			List<Article> articles = new ArrayList<Article>();

			if (titre == null)
				articleRepository.findAll().forEach(articles::add);
			else
				articleRepository.findByTitreContaining(titre).forEach(articles::add);

			if (articles.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(articles, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/pending")
	public ResponseEntity<List<Article>> getArticlesPublishedAndNotValidated(@RequestParam(required = false) String titre) {
		try {
			List<Article> articles = new ArrayList<Article>();
			articleRepository.findByIsPublishedAndNotValidated().forEach(articles::add);

			if (articles.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(articles, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping("/reject/{id}")
    public ResponseEntity<Article> rejectArticle(@PathVariable("id") long id, @RequestBody Article article)    {
		if (articles == null) {
			articles = new HashMap<>();
		}
		Article rejectedArticle = articleRepository.findById(id).get();
		rejectedArticle.setEstPublie(false);
		articleRepository.save(rejectedArticle);

        return new ResponseEntity<>(rejectedArticle, HttpStatus.OK);  
    }
	
	@PutMapping("/validate/{id}")
    public ResponseEntity<Article>validateArticle(@PathVariable("id") long id, @RequestBody Article article)    {
		if (articles == null) {
			articles = new HashMap<>();
		}
		Article validatedArticle = articleRepository.findById(id).get();
		validatedArticle.setEstValide(true);
		articleRepository.save(validatedArticle);

        return new ResponseEntity<>(validatedArticle, HttpStatus.OK);  
    }

	@GetMapping("/{id}")
	public ResponseEntity<Article> getArticleById(@PathVariable("id") long id) {
		Optional<Article> articleData = articleRepository.findById(id);

		if (articleData.isPresent()) {
			return new ResponseEntity<>(articleData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	

	@PostMapping("/creation")
//	@ResponseBody
	public ResponseEntity<Article> createArticle( @RequestBody Article article) {
		
		try {
			Article a = new Article();
			
			a.setContenu(article.getContenu());
			a.setDateCreation(LocalDate.now());
			a.setDateDerniereModif(LocalDate.now());
			a.setDescription(article.getDescription());
			a.setEstPromu(false);
			a.setEstPublie(article.getEstPublie());
			a.setEstValide(false);
			a.setTitre(article.getTitre());
			
			//suppose qu'on ne peut pas voter pour son propre article 
			a.setVote(null);

			List<Framework> listFram = new ArrayList<Framework>();
			for (Framework frameworkItere : article.getFramework()) {
				if (frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()) != null) {
					listFram.add(frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()));
				}
				else {
					Framework newFram = new Framework();
					newFram.setFramework(frameworkItere.getFramework());
					newFram.setVersion(frameworkItere.getVersion());
					frameworkRepository.save(newFram);
					listFram.add(frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()));
				}
			}
			a.setFramework(listFram);
			

			List<Langage> listLang = new ArrayList<Langage>();
			for (Langage langageItere : article.getLangage()) {
				
				if (langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()) != null) {
					listLang.add(langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()));
				}
				else {
					Langage newLang = new Langage();
					newLang.setLang(langageItere.getLang());
					newLang.setVersion(langageItere.getVersion());
					langageRepository.save(newLang);
					listLang.add(langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()));
				}				
			}
			a.setLangage(listLang);
			
			
//			a.setCategorie(categorieRepository.findByLibCategorieEquals(article.getCategorie().getLibCategorie()));
			if (categorieRepository.findByLibCategorieEquals(article.getCategorie().getLibCategorie()) != null) {
				a.setCategorie(categorieRepository.findByLibCategorieEquals(article.getCategorie().getLibCategorie()));			}
			else {
				Categorie newCat = new Categorie();
				newCat.setLibCategorie(article.getCategorie().getLibCategorie());
				categorieRepository.save(newCat);
				a.setCategorie(categorieRepository.findByLibCategorieEquals(article.getCategorie().getLibCategorie()));
			}		
//			
//			a.setType(typeRepository.findByLibTypeEquals(article.getType().getLibType()));
			if (typeRepository.findByLibTypeEquals(article.getType().getLibType()) != null) {
				a.setType(typeRepository.findByLibTypeEquals(article.getType().getLibType()));			}
			else {
				Type newTyp = new Type();
				newTyp.setLibType(article.getType().getLibType());
				typeRepository.save(newTyp);
				a.setType(typeRepository.findByLibTypeEquals(article.getType().getLibType()));
			}	

			
			
			
			a.setAuteur(utilisateurRepository.getOne(article.getAuteur().getIdUtilisateur()));

			
			articleRepository.save(a);
			
			return new ResponseEntity<>(a, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e);
			return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
		}
	}	

	
	
	@PutMapping("/modification/{id}")
	public ResponseEntity<Article> updateArticle(@PathVariable("id") long id, @RequestBody Article articleUpdated) {
		Optional<Article> articleData = articleRepository.findById(id);

		if (articleData.isPresent()) {

			Article _article = articleData.get();

			if (articleUpdated.getTitre() != null) {
				_article.setTitre(articleUpdated.getTitre());
			}
			if (articleUpdated.getDescription() != null) {
				_article.setDescription(articleUpdated.getDescription());
			}
			if (articleUpdated.getContenu() != null) {
				_article.setContenu(articleUpdated.getContenu());
			}
			if (articleUpdated.getDateDerniereModif() != null) {
				_article.setDateDerniereModif(LocalDate.now());
			}
			if (articleUpdated.getEstPublie()) {
				_article.setEstPublie(articleUpdated.getEstPublie());
			} else {
				_article.setEstPublie(false);
			}
			if (articleUpdated.getEstPromu()) {
				_article.setEstPromu(articleUpdated.getEstPromu());
			} else {
				_article.setEstPromu(false);
			}
			if (articleUpdated.getEstValide()) {
				_article.setEstValide(articleUpdated.getEstValide());
			} else {
				_article.setEstValide(false);
			}
			if (articleUpdated.getVote() != null) {
//				List<Vote> listVote = new ArrayList<Vote>();
//				for (Vote voteItere : articleUpdated.getVote()) {
//					
//					if (voteItere.getIdVote() != null) {
//						voteRepository.save(voteItere);
//						listVote.add(voteRepository.findByLikedAndCommentaireAndUtilisateurEquals(voteItere.getLiked(), voteItere.getCommentaire(), voteItere.getUtilisateur()));
//					}
//					else {
//						Vote newVote = new Vote();
//						newVote.setCommentaire(voteItere.getCommentaire());
//						newVote.setLiked(voteItere.getLiked());
//						newVote.setUtilisateur(voteItere.getUtilisateur());
//						voteRepository.save(newVote);
//						listVote.add(voteRepository.findByLikedAndCommentaireAndUtilisateurEquals(voteItere.getLiked(), voteItere.getCommentaire(), voteItere.getUtilisateur()));
//					}				
//				}
				_article.setVote(articleUpdated.getVote());				
			}
			if (articleUpdated.getLangage() != null) {
				List<Langage> listLang = new ArrayList<Langage>();
				for (Langage langageItere : articleUpdated.getLangage()) {
					
					if (langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()) != null) {
						listLang.add(langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()));
					}
					else {
						Langage newLang = new Langage();
						newLang.setLang(langageItere.getLang());
						newLang.setVersion(langageItere.getVersion());
						langageRepository.save(newLang);
						listLang.add(langageRepository.findByLangAndVersionEquals(langageItere.getLang(), langageItere.getVersion()));
					}				
				}
				_article.setLangage(listLang);
				
			}
			if (articleUpdated.getFramework() != null) {
				
				List<Framework> listFram = new ArrayList<Framework>();
				for (Framework frameworkItere : articleUpdated.getFramework()) {
					if (frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()) != null) {
						listFram.add(frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()));
					}
					else {
						Framework newFram = new Framework();
						newFram.setFramework(frameworkItere.getFramework());
						newFram.setVersion(frameworkItere.getVersion());
						frameworkRepository.save(newFram);
						listFram.add(frameworkRepository.findByFrameworkAndVersionEquals(frameworkItere.getFramework(), frameworkItere.getVersion()));
					}
				}
				_article.setFramework(listFram);				
			}
			if (articleUpdated.getType() != null) {
				if (typeRepository.findByLibTypeEquals(articleUpdated.getType().getLibType()) != null) {
					_article.setType(typeRepository.findByLibTypeEquals(articleUpdated.getType().getLibType()));			}
				else {
					Type newTyp = new Type();
					newTyp.setLibType(articleUpdated.getType().getLibType());
					typeRepository.save(newTyp);
					_article.setType(typeRepository.findByLibTypeEquals(articleUpdated.getType().getLibType()));
				}	
								
			}
			if (articleUpdated.getCategorie() != null) {
				if (categorieRepository.findByLibCategorieEquals(articleUpdated.getCategorie().getLibCategorie()) != null) {
					_article.setCategorie(categorieRepository.findByLibCategorieEquals(articleUpdated.getCategorie().getLibCategorie()));			}
				else {
					Categorie newCat = new Categorie();
					newCat.setLibCategorie(articleUpdated.getCategorie().getLibCategorie());
					categorieRepository.save(newCat);
					_article.setCategorie(categorieRepository.findByLibCategorieEquals(articleUpdated.getCategorie().getLibCategorie()));
				}					
			}
			
			return new ResponseEntity<>(articleRepository.save(_article), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/suppression/{id}")
	public ResponseEntity<HttpStatus> deleteArticle(@PathVariable("id") long id) {
		try {
			articleRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
		}
	}

	@DeleteMapping("/suppression/all")
	public ResponseEntity<HttpStatus> deleteAllArticles() {
		try {
			articleRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	
	@GetMapping("/mesarticles")
	public ResponseEntity<List<Article>> getAllArticlesOfUser(@PathVariable("userid") long id) {
		
		Optional<Utilisateur> user = utilisateurRepository.findById(id);
		List<Article> tousMesArticles = new ArrayList<Article>();
		
		if (user.isPresent()) {
			List<Article> tous = articleRepository.findAll();
			for (Article article : tous) {
				if(article.getAuteur().getIdUtilisateur() == id) {
					tousMesArticles.add(article);
				}
			}
			
			return new ResponseEntity<>(tousMesArticles, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
