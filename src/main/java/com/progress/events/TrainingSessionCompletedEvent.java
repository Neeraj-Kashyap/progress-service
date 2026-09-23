package com.progress.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Training session has been marked complete by the training service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSessionCompletedEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("traineeId")
    private String traineeId;

    @JsonProperty("completedAt")
    private Instant completedAt;

    @JsonProperty("source")
    private String source;

    @JsonProperty("schemaVersion")
    private String schemaVersion;

    /**
     * Factory method with sensible defaults.
     */
    public static com.progress.events.TrainingSessionCompletedEvent of(String sessionId, String traineeId) {
        return com.progress.events.TrainingSessionCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .traineeId(traineeId)
                .completedAt(Instant.now())
                .source("progress-service")
                .schemaVersion("1.0")
                .build();
    }
}