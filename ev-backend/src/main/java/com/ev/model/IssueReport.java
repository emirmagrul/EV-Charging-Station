package com.ev.model;

import com.ev.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description; // Arızanın detayı

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private EVDriver reporter; // Sorunu bildiren sürücü

    @ManyToOne
    @JoinColumn(name = "charger_id")
    private Charger targetCharger; // Arızalı olan cihaz

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
        if (status == null) {
            status = ReportStatus.PENDING;
        }
    }
}
