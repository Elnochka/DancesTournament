package com.dances.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "skating_results")
public class SkatingResult extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantTournament participantTournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryTournament categoryTournament;

    @Column(
            name = "final_place",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal finalPlace;

    @Column(name = "rule_used")
    private String ruleUsed = "skating";

}
