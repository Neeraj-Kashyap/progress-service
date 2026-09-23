package com.progress.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Trainee progress details.
 */
@Entity
@Table(name = "trainee_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraineeProgress {

    @Id
    @Column(name = "trainee_id")
    private String traineeId;

    @Column(name = "completed_sessions", nullable = false)
    private Integer completedSessions;

    @Column(name = "total_hours", nullable = false)
    private Double totalHours;

    @Column(name = "last_session_completed_at")
    private Instant lastSessionCompletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.completedSessions == null) {
            this.completedSessions = 0;
        }
        if (this.totalHours == null) {
            this.totalHours = 0.0;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Record completion of a training session.
     * Called when TRAINING_SESSION_COMPLETED event is received.
     */
    public void recordSessionCompletion(Instant completedAt, Double sessionHours) {
        this.completedSessions++;
        this.totalHours += (sessionHours != null ? sessionHours : 0.0);
        this.lastSessionCompletedAt = completedAt;
    }
}