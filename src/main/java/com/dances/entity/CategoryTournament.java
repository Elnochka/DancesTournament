package com.dances.entity;

import com.dances.model.SoloCoupleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category_tournaments")
public class CategoryTournament extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_category")
    private String nameCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "soloCouple", nullable = false)
    private SoloCoupleEnum soloCouple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klass_id", nullable = false)
    private Klass klass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_participant_id", nullable = false)
    private CategoryParticipant categoryParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kind_of_dance_id", nullable = false)
    private KindOfDance kindOfDance;

}
