package com.dances.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "couples")
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_couple")
    private String nameCouple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_f_id", nullable = false)
    private Participant participantFemale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_m_id", nullable = false)
    private Participant participantMale;

}
