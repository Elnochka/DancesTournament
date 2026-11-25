package com.dances.entity;

import com.dances.model.SoloCoupleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "participant_tournaments")
public class ParticipantTournament extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Стартовий номер учасника в категорії */
    @Column(name = "start_number", nullable = false)
    private Integer startNumber;

    /** Один учасник може виступати у багатьох категоріях */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    /** Категорія турніру (Solo/Group/Pair визначається там) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_tournament_id", nullable = false)
    private CategoryTournament categoryTournament;

    /** Якщо виступає в парі — зв’язок з Couple (optional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Enumerated(EnumType.STRING)
    @Column(name = "solo_couple", nullable = false)
    private SoloCoupleEnum soloCouple;

    public boolean isSolo() { return soloCouple == SoloCoupleEnum.SOLO; }

    public boolean isCouple() { return soloCouple == SoloCoupleEnum.COUPLE; }

    /** Єдине безпечне імʼя для UI/Skating */
    public String getDisplayName() {
        if (isSolo() && participant != null) {
            return participant.getFirstnameUkr() + " " + participant.getLastnameUkr();
        }
        if (isCouple() && couple != null) {
            return couple.getParticipantMale().getFirstnameUkr() + " " + couple.getParticipantMale().getLastnameUkr()
                    + " & "
                    + couple.getParticipantFemale().getFirstnameUkr() + " " + couple.getParticipantFemale().getLastnameUkr();
        }
        return "UNKNOWN";
    }

    /** Для алгоритмів, які працюють з переліком людей */
    public List<Participant> getAllHumanParticipants() {
        if (isSolo()) {
            return List.of(participant);
        }
        if (isCouple()) {
            return List.of(couple.getParticipantMale(), couple.getParticipantFemale());
        }
        return List.of();
    }
}
