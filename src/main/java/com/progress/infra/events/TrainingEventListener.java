package com.progress.infra.events;

import com.progress.events.TrainingSessionCompletedEvent;
import com.progress.application.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens to training events from Kafka.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TrainingEventListener {

    private final ProgressService progressService;

    /**
     * Listen to TRAINING_SESSION_COMPLETED events.
     *
     * @param event the deserialized event payload
     * @param eventId header: unique event identifier (for tracing)
     * @param partition Kafka partition this message came from
     * @param offset Kafka offset
     */
    @KafkaListener(
            topics = "training-events",
            groupId = "progress-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTrainingSessionCompleted(
            @Payload TrainingSessionCompletedEvent event,
            @Header(name = "eventId", required = false) String eventId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        String correlationId = eventId != null ? eventId : event.getEventId();

        log.info("Received event: type=TRAINING_SESSION_COMPLETED, eventId={}, "
                        + "traineeId={}, partition={}, offset={}, correlationId={}",
                event.getEventId(), event.getTraineeId(), partition, offset, correlationId);

        try {
            // Process the event
            progressService.processTrainingCompletion(event);

            log.info("Successfully processed event: eventId={}, traineeId={}",
                    event.getEventId(), event.getTraineeId());

        } catch (ProgressService.ProgressUpdateException e) {
            log.error("Failed to process training event: eventId={}, traineeId={}, "
                            + "will retry per configuration",
                    event.getEventId(), event.getTraineeId(), e);

            throw new EventProcessingException(
                    String.format("Failed to update progress for trainee %s", event.getTraineeId()), e
            );
        }
    }

    /**
     * Exception related to the Event Progressing
     */
    public static class EventProcessingException extends RuntimeException {
        public EventProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}