package com.news.newsback.domain.scheduler.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "scheduler_error_logs",
        indexes = {
                @Index(name = "idx_scheduler_error_logs_occurred_at", columnList = "occurred_at"),
                @Index(name = "idx_scheduler_error_logs_method_name", columnList = "method_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SchedulerErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduler_name", nullable = false, length = 100)
    private String schedulerName;

    @Column(name = "method_name", nullable = false, length = 150)
    private String methodName;

    @Column(name = "error_type", nullable = false, length = 255)
    private String errorType;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public static SchedulerErrorLog create(
            String schedulerName,
            String methodName,
            Throwable error,
            String context
    ) {
        return SchedulerErrorLog.builder()
                .schedulerName(schedulerName)
                .methodName(methodName)
                .errorType(error.getClass().getName())
                .errorMessage(error.getMessage())
                .stackTrace(toStackTrace(error))
                .context(context)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private static String toStackTrace(Throwable error) {
        StringBuilder builder = new StringBuilder();
        Throwable current = error;
        while (current != null) {
            builder.append(current).append(System.lineSeparator());
            for (StackTraceElement element : current.getStackTrace()) {
                builder.append("\tat ").append(element).append(System.lineSeparator());
            }
            current = current.getCause();
            if (current != null) {
                builder.append("Caused by: ");
            }
        }
        return builder.toString();
    }
}
