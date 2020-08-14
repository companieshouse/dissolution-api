package uk.gov.companieshouse.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DissolutionRepository extends MongoRepository<Dissolution, String> {

    Dissolution insert(Dissolution dissolution);

    Optional<Dissolution> findByCompanyNumber(String companyNumber);

    @Query("{'submission.status': 'PENDING', $or : [{'submission.date_time': {'$gte' : ?0}}, {'submission.date_time': null}]}")
    List<Dissolution> findPendingDissolutions(LocalDateTime dateTime, Pageable limit);
}
