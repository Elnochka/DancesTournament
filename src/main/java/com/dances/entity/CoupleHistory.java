package com.dances.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "couple_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Пара, яка була створена або змінена
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_id", nullable = false)
    private Participant maleParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_id", nullable = false)
    private Participant femaleParticipant;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
