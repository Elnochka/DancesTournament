package com.dances.service.impl;

import com.dances.entity.*;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.entity.dto.SkatingResultDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.*;
import com.dances.service.FinalMarkService;
import com.dances.service.MarkService;
import com.dances.service.ParticipantTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkatingService {

    private final FinalMarkRepository finalMarkRepository;
    private final SkatingResultsRepository skatingResultsRepository;
    private final MarkRepository markRepository;
    private final CategoryTournamentRepository categoryRepository;
    private final JudgeRepository judgeRepository;
    private final RoundSettingsRepository roundSettingsRepository;
    private final RoundSettingsServiceImpl roundSettingsService;
    private final MarkService markService;
    private final ParticipantTournamentService participantService;
    private final FinalMarkService finalMarkService;
    private final DanceRepository danceRepository;

    @Transactional
    public void lockDance(Long categoryId, Long roundId, Long danceId) {

        CategoryTournament categoryTournament = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("CategoryTournament not found"));

        RoundSettings roundSetting = roundSettingsRepository.findById(roundId)
                .orElseThrow(() -> new IllegalArgumentException("RoundSettings not found"));
        RoundEnum round = roundSetting.getRound();

        boolean social = isSocialCategory(categoryTournament);

        if (!social) {

            if (round == RoundEnum.FINAL){

                validatePlace0ForDance(categoryId, danceId);

                finalMarkRepository.lockDanceRound(categoryId, danceId);

            } else {

                validatePassedForDance(categoryId, danceId, roundSetting);

                markRepository.lockDanceRound(categoryId, round, danceId);

            }

        }
    }

    private void validatePlace0ForDance(Long categoryId, Long danceId){

        boolean hasUnassignedPlaces = finalMarkRepository.existsByCategoryTournamentIdAndDanceIdAndPlace(
                        categoryId,
                        danceId,
                        0
                );

        if (hasUnassignedPlaces) {
            throw new IllegalStateException(
                    "Cannot lock dance: some final marks have place = 0"
            );
        }

    }

    private void validatePassedForDance(Long categoryId, Long danceId, RoundSettings roundSetting){

        RoundEnum round = roundSetting.getRound();

        int couplesToNextRound = roundSetting.getCouplesToNextRound();

        List<Judge> judges = findJudges();

        for (Judge judge: judges) {

            long passedCount = markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                    categoryId,
                    round,
                    judge.getId(),
                    danceId

            );

            if ((passedCount < couplesToNextRound) || (passedCount > couplesToNextRound)) {
                throw new IllegalStateException(
                        "Not enough or more than need passed participants. Required: "
                                + couplesToNextRound
                                + ", actual: "
                                + passedCount
                                + ", judge: "
                                + judge.getNameJudge()
                );
            }
        }

    }

    private void validatePassedBeforeNextRound(CategoryTournament category, RoundSettings roundSettings, boolean social){

        List<Dance> dances = social
                ? Collections.emptyList()
                : danceRepository.findByKindOfDanceId(category.getKindOfDance().getId());

        for (Dance dance: dances) {

            validatePassedForDance(category.getId(), dance.getId(), roundSettings);

        }

    }

    private List<Judge> findJudges() {
        LocalDateTime now = LocalDateTime.now();

        return judgeRepository.findAllByCreatedAtBetween(
                now.minusDays(7),
                now
        );
    }

    @Transactional
    public void createMarksOrFinalMarksForCategoryAndRound(Long categoryId, Long roundId) {

        log.info(
                "Creating marks: category={}, roundId={}",
                categoryId,
                roundId
        );

        CategoryTournament category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "CategoryTournament not found: " + categoryId
                        )
                );

        RoundSettings roundSettings = roundSettingsRepository.findById(roundId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "RoundSettings not found: " + roundId
                                )
                        );

        if (!roundSettings.getCategoryTournament().getId().equals(categoryId)) {
            throw new IllegalArgumentException(
                    "RoundSettings does not belong to category: " + categoryId
            );
        }

        boolean social = isSocialCategory(category);

        validatePassedBeforeNextRound(category, roundSettings, social);

        List<Judge> judges = findJudges();

        if (judges.isEmpty()) {

            throw new IllegalArgumentException("List of the active judges is empty!");

        }

        RoundEnum nextRound = roundSettingsService.resolveNextRound(categoryId, roundSettings);



        List<ParticipantTournamentDto> participants = participantService.selectParticipants(category, roundSettings,
                                                        social);

        markService.createMarks(
                category,
                nextRound,
                participants,
                judges,
                social
        );

        log.info(
                "Marks created successfully: category={}, currentRound={}, " +
                        "nextRound={}, participants={}, judges={}",
                categoryId,
                roundSettings.getRound(),
                nextRound,
                participants.size(),
                judges.size()
        );
    }

    private boolean isSocialCategory(CategoryTournament category) {
        return category.getNameCategory() != null
                && category.getNameCategory()
                .toLowerCase(Locale.ROOT)
                .contains("social");
    }

    private Map<ParticipantTournament, BigDecimal> computeSkating(List<FinalMark> marks) {

        if (marks.isEmpty()) {
            return Map.of();
        }

        finalMarkService.validateSingleDanceFinalMarks(marks);

        return computeSkatingCore(marks);

    }

    private Map<ParticipantTournament, BigDecimal> computeSkatingCore(List<FinalMark> marks) {

        if (marks.isEmpty()) {
            return Map.of();
        }

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                marks.stream()
                        .collect(Collectors.groupingBy(
                                FinalMark::getParticipantTournament,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        int marksCount = marksByParticipant.values()
                .iterator()
                .next()
                .size();

        int majority = marksCount / 2 + 1;

        int maxColumn = marks.stream()
                .mapToInt(FinalMark::getPlace)
                .max()
                .orElseThrow();

        List<ParticipantTournament> remaining =
                new ArrayList<>(marksByParticipant.keySet());

        Map<ParticipantTournament, BigDecimal> result =
                new LinkedHashMap<>();

        int placeToAssign = 1;

        while (!remaining.isEmpty()) {

            List<ParticipantTournament> orderedCandidates =
                    findCandidatesForPlace(
                            remaining,
                            marksByParticipant,
                            majority,
                            maxColumn
                    );

            if (orderedCandidates.isEmpty()) {
                throw new IllegalStateException(
                        "Unable to calculate place " + placeToAssign
                );
            }

            int groupSize = orderedCandidates.size();

            BigDecimal averagePlace = BigDecimal.valueOf(2L * placeToAssign + groupSize - 1)
                    .divide(BigDecimal.valueOf(2));

            for (ParticipantTournament participant : orderedCandidates) {
                result.put(participant, averagePlace);
                remaining.remove(participant);
            }

            placeToAssign += groupSize;

        }

        return result;
    }

    private List<ParticipantTournament> findCandidatesForPlace(
            List<ParticipantTournament> remaining,
            Map<ParticipantTournament, List<FinalMark>> marksByParticipant,
            int majority,
            int maxColumn
    ) {

        for (int column = 1; column <= maxColumn; column++) {

            Map<ParticipantTournament, Integer> majorityCounts =
                    new LinkedHashMap<>();

            for (ParticipantTournament participant : remaining) {

                int count = countMarksAtOrBetter(
                        marksByParticipant.get(participant),
                        column
                );

                if (count >= majority) {
                    majorityCounts.put(participant, count);
                }
            }

            /*
             * Rule 8:
             *
             * Якщо ніхто не має majority у цій колонці,
             * переходимо до наступної.
             */
            if (majorityCounts.isEmpty()) {
                continue;
            }

            /*
             * Rule 6:
             *
             * Беремо найбільшу majority.
             */
            int greatestMajority =
                    Collections.max(majorityCounts.values());

            List<ParticipantTournament> candidates =
                    majorityCounts.entrySet()
                            .stream()
                            .filter(entry ->
                                    entry.getValue() == greatestMajority
                            )
                            .map(Map.Entry::getKey)
                            .toList();

            /*
             * Тільки одна пара має найбільшу majority.
             */
            if (candidates.size() == 1) {
                return List.of(candidates.getFirst());
            }

            /*
             * Rule 7:
             *
             * Декілька пар мають однакову majority.
             */
            return resolveRule7Tie(
                    candidates,
                    marksByParticipant,
                    majority,
                    column,
                    maxColumn
            );
        }

        return List.of();
    }

    private int countMarksAtOrBetter(
            List<FinalMark> marks,
            int column
    ) {

        return (int) marks.stream()
                .filter(mark -> mark.getPlace() <= column)
                .count();
    }

    private List<ParticipantTournament> resolveRule7Tie(
            List<ParticipantTournament> candidates,
            Map<ParticipantTournament, List<FinalMark>> marksByParticipant,
            int majority,
            int currentColumn,
            int maxColumn
    ) {

        List<ParticipantTournament> current =
                new ArrayList<>(candidates);

        for (
                int column = currentColumn;
                column <= maxColumn;
                column++
        ) {

            Map<ParticipantTournament, Integer> counts =
                    new LinkedHashMap<>();

            for (ParticipantTournament participant : current) {

                int count = countMarksAtOrBetter(
                        marksByParticipant.get(participant),
                        column
                );

                counts.put(participant, count);
            }

            /*
             * Rule 6 inside Rule 7:
             *
             * Якщо після переходу до наступної колонки
             * majority стала різною — більша majority перемагає.
             */
            int maxCount =
                    Collections.max(counts.values());

            /*
             * Ніхто не має majority.
             * Переходимо до наступної колонки.
             */
            if (maxCount < majority) {
                continue;
            }

            List<ParticipantTournament> withMaxCount =
                    current.stream()
                            .filter(p ->
                                    counts.get(p) == maxCount
                            )
                            .toList();

            if (withMaxCount.size() == 1) {
                return List.of(withMaxCount.getFirst());
            }

            /*
             * Якщо однакова majority,
             * використовуємо Rule 7 — суму marks.
             */
            Map<ParticipantTournament, Integer> sums =
                    new LinkedHashMap<>();

            for (ParticipantTournament participant : withMaxCount) {

                int sum = sumMarksAtOrBetter(
                        marksByParticipant.get(participant),
                        column
                );

                sums.put(participant, sum);
            }

            int lowestSum =
                    Collections.min(sums.values());

            List<ParticipantTournament> withLowestSum =
                    withMaxCount.stream()
                            .filter(p ->
                                    sums.get(p) == lowestSum
                            )
                            .toList();

            /*
             * Сума розділила кандидатів.
             */
            if (withLowestSum.size() == 1) {
                return List.of(withLowestSum.getFirst());
            }

            /*
             * Same majority + same sum.
             *
             * Rule 7 каже перейти до наступної колонки
             * ТІЛЬКИ для цих кандидатів.
             */
            current = new ArrayList<>(withLowestSum);
        }

        /*
         * Абсолютна нічия.
         *
         * Формально тут повинно бути середнє
         * місць, наприклад 3.5.

         */

        return current;
    }

    private int sumMarksAtOrBetter(
            List<FinalMark> marks,
            int column
    ) {

        return marks.stream()
                .filter(mark -> mark.getPlace() <= column)
                .mapToInt(FinalMark::getPlace)
                .sum();
    }

    private record MultiDanceResults(
            List<Long> danceIds,
            Map<Long, Map<ParticipantTournament, BigDecimal>> placesByDance,
            Map<ParticipantTournament, BigDecimal> totalPlaces
    ) {}

    private List<ParticipantTournament> resolveRule10Tie(
            List<ParticipantTournament> tiedParticipants,
            Map<Long, Map<ParticipantTournament, BigDecimal>> placesByDance,
            int placeToAssign
    ) {
        Map<ParticipantTournament, Long> counts =
                new LinkedHashMap<>();

        Map<ParticipantTournament, BigDecimal> sums =
                new LinkedHashMap<>();

        for (ParticipantTournament participant : tiedParticipants) {
            List<BigDecimal> places = placesByDance.values()
                    .stream()
                    .map(results -> results.get(participant))
                    .filter(Objects::nonNull)
                    .toList();

            long count = places.stream()
                    .filter(place ->
                            place.compareTo(
                                    BigDecimal.valueOf(placeToAssign)
                            ) <= 0
                    )
                    .count();

            BigDecimal sum = places.stream()
                    .filter(place ->
                            place.compareTo(
                                    BigDecimal.valueOf(placeToAssign)
                            ) <= 0
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            counts.put(participant, count);
            sums.put(participant, sum);
        }

        long greatestCount = counts.values()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        List<ParticipantTournament> bestByCount =
                tiedParticipants.stream()
                        .filter(p -> counts.get(p) == greatestCount)
                        .toList();

        if (bestByCount.size() == 1) {
            return bestByCount;
        }

        BigDecimal lowestSum = bestByCount.stream()
                .map(sums::get)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<ParticipantTournament> bestBySum =
                bestByCount.stream()
                        .filter(p ->
                                sums.get(p).compareTo(lowestSum) == 0
                        )
                        .toList();

        return bestBySum;
    }

    private List<ParticipantTournament> resolveRule11Tie(List<ParticipantTournament> tiedParticipants,
                                                         List<FinalMark> allDanceMarks) {

        if (tiedParticipants.size() < 2) {
            return tiedParticipants;
        }

        Set<ParticipantTournament> tiedSet =
                new HashSet<>(tiedParticipants);

        List<FinalMark> marksForTie = allDanceMarks.stream()
                .filter(mark -> tiedSet.contains(
                        mark.getParticipantTournament()
                ))
                .toList();

        // Усі відмітки всіх танців обробляються як один набір.
        Map<ParticipantTournament, BigDecimal> skatingPlaces =
                computeSkatingCore(marksForTie);

        BigDecimal bestPlace = tiedParticipants.stream()
                .map(skatingPlaces::get)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to resolve Rule 11 tie"
                ));

        return tiedParticipants.stream()
                .filter(participant -> {
                    BigDecimal place = skatingPlaces.get(participant);

                    return place != null
                            && place.compareTo(bestPlace) == 0;
                })
                .toList();
    }

    @Transactional
    public List<SkatingResultDto> calculateFinalPlaces(Long categoryId) {

        List<FinalMark> marks =
                finalMarkRepository.findByCategoryTournamentId(categoryId);

        if (marks.isEmpty()) {
            throw new NotFoundException(
                    "No final marks found for category " + categoryId
            );
        }

        boolean hasDance = marks.stream()
                .anyMatch(mark -> mark.getDance() != null);

        boolean hasNullDance = marks.stream()
                .anyMatch(mark -> mark.getDance() == null);

        if (hasDance && hasNullDance) {
            throw new IllegalStateException(
                    "Final marks cannot mix single-dance and multi-dance marks"
            );
        }

        if (hasNullDance) {
            finalMarkRepository.lockRound(categoryId);
        }

        Map<ParticipantTournament, BigDecimal> computed;

        if (hasDance) {

            validateMultiDanceMarks(marks);
            computed = computeMultiDanceFinalPlaces(marks);

        } else {
            computed = computeSkating(marks);
        }

        skatingResultsRepository.deleteByCategoryTournamentId(categoryId);

        List<SkatingResult> results = computed.entrySet()
                .stream()
                .map(entry -> {
                    SkatingResult result = new SkatingResult();

                    result.setParticipantTournament(entry.getKey());
                    result.setCategoryTournament(
                            entry.getKey().getCategoryTournament()
                    );
                    result.setFinalPlace(entry.getValue());

                    return skatingResultsRepository.save(result);
                })
                .toList();

        return results.stream()
                .map(SkatingResultDto::fromEntity)
                .toList();
    }

    private void validateMultiDanceMarks(List<FinalMark> marks) {

        Map<Long, List<FinalMark>> marksByDance = marks.stream()
                .collect(Collectors.groupingBy(
                        mark -> mark.getDance().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        if (marksByDance.isEmpty()) {
            throw new IllegalStateException("No dances found");
        }

        Set<Long> expectedParticipantIds = null;
        Set<Long> expectedJudgeIds = null;

        for (Map.Entry<Long, List<FinalMark>> entry
                : marksByDance.entrySet()) {

            List<FinalMark> danceMarks = entry.getValue();

            Map<Long, List<FinalMark>> byParticipant = danceMarks.stream()
                    .collect(Collectors.groupingBy(
                            mark -> mark.getParticipantTournament().getId()
                    ));

            Set<Long> participantIds = byParticipant.keySet();

            if (expectedParticipantIds == null) {
                expectedParticipantIds = new HashSet<>(participantIds);
            } else if (!expectedParticipantIds.equals(participantIds)) {
                throw new IllegalStateException(
                        "Different participant sets between dances"
                );
            }

            Set<Long> judgeIds = danceMarks.stream()
                    .map(FinalMark::getJudge)
                    .filter(Objects::nonNull)
                    .map(Judge::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (expectedJudgeIds == null) {
                expectedJudgeIds = new HashSet<>(judgeIds);
            } else if (!expectedJudgeIds.equals(judgeIds)) {
                throw new IllegalStateException(
                        "Different judge sets between dances"
                );
            }

            int participantCount = participantIds.size();

            for (Map.Entry<Long, List<FinalMark>> participantEntry
                    : byParticipant.entrySet()) {

                List<FinalMark> participantMarks =
                        participantEntry.getValue();

                Set<Long> participantJudgeIds = new HashSet<>();

                for (FinalMark mark : participantMarks) {
                    if (mark.getJudge() == null
                            || mark.getJudge().getId() == null) {
                        throw new IllegalStateException(
                                "Judge is missing for final mark " + mark.getId()
                        );
                    }

                    if (!participantJudgeIds.add(mark.getJudge().getId())) {
                        throw new IllegalStateException(
                                "Duplicate judge mark for participant "
                                        + participantEntry.getKey()
                                        + " in dance " + entry.getKey()
                        );
                    }

                    if (mark.getPlace() < 1
                            || mark.getPlace() > participantCount) {
                        throw new IllegalStateException(
                                "Invalid place " + mark.getPlace()
                                        + " for participant "
                                        + participantEntry.getKey()
                        );
                    }
                }

                if (participantJudgeIds.size() != judgeIds.size()) {
                    throw new IllegalStateException(
                            "Participant " + participantEntry.getKey()
                                    + " does not have marks from all judges"
                    );
                }
            }
        }
    }

    private MultiDanceResults calculateMultiDanceResults(List<FinalMark> marks) {

        Map<Long, List<FinalMark>> marksByDance = marks.stream()
                .collect(Collectors.groupingBy(
                        mark -> mark.getDance().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, Map<ParticipantTournament, BigDecimal>> placesByDance =
                new LinkedHashMap<>();

        Map<ParticipantTournament, BigDecimal> totalPlaces =
                new LinkedHashMap<>();

        for (Map.Entry<Long, List<FinalMark>> entry
                : marksByDance.entrySet()) {

            Map<ParticipantTournament, BigDecimal> dancePlaces =
                    computeSkating(entry.getValue());

            placesByDance.put(entry.getKey(), dancePlaces);

            dancePlaces.forEach((participant, place) ->
                    totalPlaces.merge(
                            participant,
                            place,
                            BigDecimal::add
                    )
            );
        }

        return new MultiDanceResults(
                new ArrayList<>(marksByDance.keySet()),
                placesByDance,
                totalPlaces
        );
    }

    private Map<ParticipantTournament, BigDecimal> computeMultiDanceFinalPlaces(List<FinalMark> marks) {

        MultiDanceResults multi = calculateMultiDanceResults(marks);

        Map<BigDecimal, List<ParticipantTournament>> groups =
                multi.totalPlaces().entrySet()
                        .stream()
                        .collect(Collectors.groupingBy(
                                Map.Entry::getValue,
                                TreeMap::new,
                                Collectors.mapping(
                                        Map.Entry::getKey,
                                        Collectors.toList()
                                )
                        ));

        Map<ParticipantTournament, BigDecimal> result =
                new LinkedHashMap<>();

        int placeToAssign = 1;

        for (List<ParticipantTournament> group : groups.values()) {

            List<ParticipantTournament> remaining =
                    new ArrayList<>(group);

            while (!remaining.isEmpty()) {

                List<ParticipantTournament> bestByRule10 =
                        resolveRule10Tie(
                                remaining,
                                multi.placesByDance(),
                                placeToAssign
                        );

                List<ParticipantTournament> best;

                if (bestByRule10.size() == 1) {
                    best = bestByRule10;
                } else {
                    best = resolveRule11Tie(
                            bestByRule10,
                            marks
                    );
                }

                // Якщо правила не розв'язали нічию,
                // присвоюємо учасникам спільне середнє місце.
                if (best.size() == remaining.size()) {
                    BigDecimal averagePlace = BigDecimal.valueOf(
                            2L * placeToAssign + best.size() - 1
                    ).divide(BigDecimal.valueOf(2));

                    for (ParticipantTournament participant : best) {
                        result.put(participant, averagePlace);
                    }

                    placeToAssign += best.size();
                    remaining.removeAll(best);
                    continue;
                }

                BigDecimal assignedPlace = BigDecimal.valueOf(placeToAssign);

                for (ParticipantTournament participant : best) {
                    result.put(participant, assignedPlace);
                }

                placeToAssign += best.size();
                remaining.removeAll(best);
            }
        }

        return result;
    }

    /**
     * Повертає результати після розрахунку
     */
    public List<SkatingResultDto> getResults(Long categoryId) {
        return skatingResultsRepository.findByCategoryTournamentIdOrderByFinalPlace(categoryId).stream().
                map(SkatingResultDto::fromEntity)
                .toList();

    }
}
