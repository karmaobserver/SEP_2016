package ftn.uns.ac.rs.tim6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ftn.uns.ac.rs.tim6.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>{

}
