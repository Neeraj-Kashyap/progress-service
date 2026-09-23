package com.progress.application;

import com.progress.domain.ProcessedEvent;
import com.progress.infra.repository.ProcessedEventRepository;
import com.progress.events.TrainingSessionCompletedEvent;
import com.progress.domain.TraineeProgress;
import com.progress.infra.repository.TraineeProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for processing training completion events and updating progress.
 *
 * Design notes:
 * - Handles idempotency via deduplication (store processed eventIds)
 * - Creates trainee progress record if doesn't exist (upsert pattern)
 * - Transactional to ensure consistency
 * - Error handling allows for retry logic in consumer
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProgressService {

    private final TraineeProgressRepository progressRepository;
    private final ProcessedEventRepository processedEventRepository;

    /**
     * Process a training session completion event.
     * Updates or creates trainee progress record.
     *
     * Handles idempotency: returns early if event was already processed.
     *
     * @param event the training session completed event
     * @throws Exception if processing fails
     */
    @Transactional
    public void processTrainingCompletion(TrainingSessionCompletedEvent event) {
        String eventId = event.getEventId();
        String traineeId = event.getTraineeId();

        // 1. Check idempotency: has this event been processed?
        if (processedEventRepository.existsById(eventId)) {
            log.info("Event {} already processed, skipping", eventId);
            return;
        }

        try {
            // 2. Get or create trainee progress
            TraineeProgress progress = progressRepository.findById(traineeId)
                    .orElseGet(() -> TraineeProgress.builder()
                            .traineeId(traineeId)
                            .completedSessions(0)
                            .totalHours(0.0)
                            .build());

            // 3. Update progress with session completion
            progress.recordSessionCompletion(event.getCompletedAt(), null);

            // 4. Persist
            progressRepository.save(progress);

            // 5. Mark event as processed (idempotency guard)
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .traineeId(traineeId)
                    .eventType("TRAINING_SESSION_COMPLETED")
                    .build());

            log.info("Processed training completion: traineeId={}, eventId={}, totalCompleted={}",
                    traineeId, eventId, progress.getCompletedSessions());

        } catch (Exception e) {
            log.error("Failed to process training completion event: {}", eventId, e);
            throw new ProgressUpdateException(
                    String.format("Failed to update progress for trainee %s", traineeId), e
            );
        }
    }

    /**
     * Get trainee progress (read-only).
     */
    @Transactional(readOnly = true)
    public TraineeProgress getProgress(String traineeId) {
        return progressRepository.findById(traineeId)
                .orElseThrow(() -> new ProgressNotFoundException(
                        String.format("Progress record for trainee %s not found", traineeId)
                ));
    }

    public static class ProgressUpdateException extends RuntimeException {
        public ProgressUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProgressNotFoundException extends RuntimeException {
        public ProgressNotFoundException(String message) {
            super(message);
        }
    }
}