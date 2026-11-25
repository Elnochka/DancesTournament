package com.dances.integration;

import com.dances.entity.*;
import com.dances.entity.dto.SkatingResultDto;
import com.dances.model.SoloCoupleEnum;
import com.dances.repository.*;
import com.dances.service.impl.SkatingService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SkatingServiceIT extends IntegrationTestBase {

    @Autowired
    private SkatingService skatingService;

    @Autowired
    private FinalMarkRepository finalMarkRepository;

    @Autowired
    private SkatingResultsRepository skatingResultsRepository;

    @Autowired
    private CategoryTournamentRepository categoryTournamentRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private KlassRepository klassRepository;

    @Autowired
    private CategoryParticipantRepository categoryParticipantRepository;

    @Autowired
    private KindOfDanceRepository kindOfDanceRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private JudgeRepository judgeRepository;

    @Autowired
    private ParticipantTournamentRepository participantTournamentRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EntityManager entityManager;

    private CategoryTournament createCategory() {
        Country country = countryRepository.save(
                Country.builder()
                        .nameCountry("Test Country")
                        .build()
        );

        Club club = clubRepository.save(
                Club.builder()
                        .nameClub("Test Club")
                        .build()
        );

        City city = cityRepository.save(
                City.builder()
                        .nameCity("Test City")
                        .build()
        );

        Klass klass = klassRepository.save(
                Klass.builder()
                        .nameKlass("Test Klass")
                        .build()
        );

        CategoryParticipant categoryParticipant =
                categoryParticipantRepository.save(
                        CategoryParticipant.builder()
                                .nameCategory("Test Age")
                                .age(18)
                                .ageon(30)
                                .build()
                );

        KindOfDance kindOfDance = kindOfDanceRepository.save(
                KindOfDance.builder()
                        .nameKindOfDance("Test Dance")
                        .fullNameKindOfDance("Test Dance")
                        .build()
        );

        return categoryTournamentRepository.save(
                CategoryTournament.builder()
                        .nameCategory("Test Category")
                        .soloCouple(SoloCoupleEnum.SOLO)
                        .klass(klass)
                        .categoryParticipant(categoryParticipant)
                        .kindOfDance(kindOfDance)
                        .build()
        );
    }

    private ParticipantTournament createParticipant(
            CategoryTournament category,
            Trainer trainer,
            Country country,
            Klass klass,
            int startNumber
    ) {
        Participant participant = participantRepository.save(
                Participant.builder()
                        .firstnameUkr("Test" + startNumber)
                        .lastnameUkr("Participant")
                        .country(country)
                        .trainer(trainer)
                        .klass(klass)
                        .build()
        );

        return participantTournamentRepository.save(
                ParticipantTournament.builder()
                        .startNumber(startNumber)
                        .participant(participant)
                        .categoryTournament(category)
                        .soloCouple(SoloCoupleEnum.SOLO)
                        .build()
        );
    }

    private Judge createJudge(Trainer trainer, int number) {
        return judgeRepository.save(
                Judge.builder()
                        .nameJudge("J" + number)
                        .trainer(trainer)
                        .build()
        );
    }

    private Trainer createTrainer(
            Club club,
            City city,
            Country country,
            int number
    ) {
        return trainerRepository.save(
                Trainer.builder()
                        .nameTrainer("Test Trainer " + number)
                        .club(club)
                        .city(city)
                        .country(country)
                        .build()
        );
    }

    private void createFinalMark(
            CategoryTournament category,
            ParticipantTournament participant,
            Judge judge,
            int place
    ) {
        finalMarkRepository.save(
                FinalMark.builder()
                        .categoryTournament(category)
                        .participantTournament(participant)
                        .judge(judge)
                        .place(place)
                        .locked(false)
                        .build()
        );
    }

    @Test
    @Transactional
    void calculateFinalPlaces_shouldCalculateAndPersistResults() {

        // Arrange
        CategoryTournament category = createCategory();

        Country country = countryRepository.findAll().getFirst();
        Club club = clubRepository.findAll().getFirst();
        City city = cityRepository.findAll().getFirst();
        Klass klass = klassRepository.findAll().getFirst();

        Trainer trainer = createTrainer(club, city, country, 1);

        ParticipantTournament participant1 =
                createParticipant(category, trainer, country, klass, 1);

        ParticipantTournament participant2 =
                createParticipant(category, trainer, country, klass, 2);

        ParticipantTournament participant3 =
                createParticipant(category, trainer, country, klass, 3);

        Judge judge1 = createJudge(trainer, 1);
        Judge judge2 = createJudge(trainer, 2);
        Judge judge3 = createJudge(trainer, 3);

        // Judge 1
        createFinalMark(category, participant1, judge1, 1);
        createFinalMark(category, participant2, judge1, 2);
        createFinalMark(category, participant3, judge1, 3);

        // Judge 2
        createFinalMark(category, participant1, judge2, 1);
        createFinalMark(category, participant2, judge2, 2);
        createFinalMark(category, participant3, judge2, 3);

        // Judge 3
        createFinalMark(category, participant1, judge3, 1);
        createFinalMark(category, participant2, judge3, 2);
        createFinalMark(category, participant3, judge3, 3);

        // Act
        List<SkatingResultDto> results =
                skatingService.calculateFinalPlaces(category.getId());

        // Assert

        entityManager.clear();

        assertTrue(
                finalMarkRepository.findByCategoryTournamentId(category.getId())
                        .stream()
                        .allMatch(FinalMark::isLocked)
        );

        List<SkatingResult> savedResults =
                skatingResultsRepository.findByCategoryTournamentIdOrderByFinalPlace(
                        category.getId()
                );

        assertEquals(3, savedResults.size());

        Map<Long, BigDecimal> placesByParticipant = savedResults.stream()
                .collect(Collectors.toMap(
                        result -> result.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(
                BigDecimal.ONE,
                placesByParticipant.get(participant1.getId())
        );

        assertEquals(
                BigDecimal.valueOf(2),
                placesByParticipant.get(participant2.getId())
        );

        assertEquals(
                BigDecimal.valueOf(3),
                placesByParticipant.get(participant3.getId())
        );

        assertTrue(
                finalMarkRepository.findByCategoryTournamentId(category.getId())
                        .stream()
                        .allMatch(FinalMark::isLocked)
        );
    }
}
