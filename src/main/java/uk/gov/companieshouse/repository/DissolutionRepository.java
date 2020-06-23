package uk.gov.companieshouse.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.model.db.Dissolution;

import java.util.Optional;

@Repository
public interface DissolutionRepository extends MongoRepository<Dissolution, String> {

    Dissolution insert(Dissolution dissolution);
    Optional<Dissolution> findByCompanyNumber(String companyNumber);
}
