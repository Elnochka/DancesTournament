package com.dances.service.impl;

import com.dances.entity.CategoryParticipant;
import com.dances.entity.CategoryTournament;
import com.dances.entity.Couple;
import com.dances.entity.Participant;
import com.dances.model.SoloCoupleEnum;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ParticipantTournamentValidator {

    public void validateParticipantMatchesCategory(
            Participant participant,
            CategoryTournament category
    ) {
        if (!Objects.equals(participant.getKlass(), category.getKlass())) {
            throw new IllegalArgumentException(
                    "Participant klass does not match category klass"
            );
        }

        int age = participant.getAge();
        CategoryParticipant required = category.getCategoryParticipant();

        if (!ageMatchesCategory(age, required)) {
            throw new IllegalArgumentException(
                    "Participant age " + age
                            + " does not match required category "
                            + required.getNameCategory()
            );
        }

        if (!participant.getKindOfDances().contains(category.getKindOfDance())) {
            throw new IllegalArgumentException(
                    "Participant does not dance required kind: "
                            + category.getKindOfDance().getNameKindOfDance()
            );
        }
    }


//    public void validateParticipantMatchesCategory(Participant participant, CategoryTournament category) {
//
//        if (!participant.getKlass().equals(category.getKlass())) {
//            throw new IllegalArgumentException("Participant klass does not match category klass");
//        }
//
//        int age = participant.getAge();
//        CategoryParticipant required = category.getCategoryParticipant();
//
//        if (!ageMatchesCategory(age, required)) {
//            throw new IllegalArgumentException(
//                    "Participant age " + age + " does not match required category " + required.getNameCategory());
//        }
//
//        if (!participant.getKindOfDances().contains(category.getKindOfDance())) {
//            throw new IllegalArgumentException(
//                    "Participant does not dance required kind: " + category.getKindOfDance().getNameKindOfDance());
//        }
//    }

    public void validateCoupleMatchesCategory(Couple couple, CategoryTournament category) {

        validateParticipantMatchesCategory(couple.getParticipantMale(), category);

        validateParticipantMatchesCategory(couple.getParticipantFemale(), category);

    }

    private boolean ageMatchesCategory(int age, CategoryParticipant categoryParticipant) {
        return age >= categoryParticipant.getAge()
                && age <= categoryParticipant.getAgeon();
    }

    public void validateSoloCouple(
            SoloCoupleEnum soloCoupleEnum,
            CategoryTournament category
    ) {
        if (soloCoupleEnum == null || soloCoupleEnum != category.getSoloCouple()) {
            throw new IllegalArgumentException(
                    "Registration type does not match category tournament type"
            );
        }
    }

}
