package com.dances.entity;

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
@Table(
        name = "final_mark",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_final_mark_judge_category_place",
                        columnNames = {
                                "category_id",
                                "judge_id",
                                "place"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_final_mark_participant_category_judge",
                        columnNames = {
                                "participant_id",
                                "category_id",
                                "judge_id"
                        }
                )
        }
)
//@Table(name = "final_mark")
public class FinalMark extends AuditableEntity {

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
    @JoinColumn(name = "judge_id", nullable = false)
    private Judge judge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dance_id")
    private Dance dance;

    @Column(name = "place", nullable = false)
    private int place;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}
