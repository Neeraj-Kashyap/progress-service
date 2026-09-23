package com.progress.infra.repository;

import com.progress.domain.TraineeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for trainee progress.
 */
@Repository
public interface TraineeProgressRepository extends JpaRepository<TraineeProgress, String> {
}