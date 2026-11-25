package com.dances.entity;

import com.dances.model.RoundEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mark")
public class Mark extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantTournament participantTournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryTournament categoryTournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dance_id")
    private Dance dance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private Judge judge;

    @Enumerated(EnumType.STRING)
    @Column(name = "round", nullable = false)
    private RoundEnum round;

    @Column(name = "is_passed", nullable = false)
    private boolean passed;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}
