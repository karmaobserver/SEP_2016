package ftn.uns.ac.rs.tim6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ftn.uns.ac.rs.tim6.model.IncomingMessage;

@Repository
public interface IncomingMessageRepository  extends JpaRepository<IncomingMessage, Long> {

}
