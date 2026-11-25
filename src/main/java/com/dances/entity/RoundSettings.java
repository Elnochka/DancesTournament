package com.dances.entity;

import com.dances.model.RoundEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "round_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_round_settings_category_round",
                        columnNames = {
                                "category_tournament_id",
                                "round"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_tournament_id", nullable = false)
    private CategoryTournament categoryTournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "round", nullable = false)
    private RoundEnum round;

    @Column(name = "round_order", nullable = false)
    private Integer roundOrder;

    @Column(name = "couples_to_next_round")
    private Integer couplesToNextRound;
}
