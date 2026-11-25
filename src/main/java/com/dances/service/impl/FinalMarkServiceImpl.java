package com.dances.service.impl;

import com.dances.entity.FinalMark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.FinalMarkDto;
import com.dances.entity.dto.FinalMarkPatchDto;
import com.dances.entity.dto.FinalMarkPatchJudgeDto;
import com.dances.entity.dto.FinalMarkRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.FinalMarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalMarkServiceImpl implements FinalMarkService {

    private final FinalMarkRepository finalMarkRepository;
    private final ParticipantTournamentRepository participantRepository;
    private final CategoryTournamentRepository categoryRepository;
    private final JudgeRepository judgeRepository;
    private final DanceRepository danceRepository;

    private void validatePlace(
            Long categoryId,
            Long judgeId,
            Long danceId,
            int place,
            Long currentFinalMarkId

    ) {

        log.debug(
                "Validating place: categoryId={}, judgeId={}, place={}, currentFinalMarkId={}",
                categoryId,
                judgeId,
                place,
                currentFinalMarkId
        );

        long participantsCount =
                finalMarkRepository.countDistinctParticipantsByCategory(categoryId);

        log.debug(
                "Finalists count for categoryId={}: {}",
                categoryId,
                participantsCount
        );

        if (place < 0 || place > participantsCount) {
            throw new IllegalArgumentException(
                    "Place must be between 1 and " + participantsCount + " or 0 "
            );
        }

        boolean duplicate = false;

        if (place > 0) {
            if (currentFinalMarkId == null) {
                if (danceId == 0L) {
                    duplicate =
                            finalMarkRepository
                                    .existsByCategoryTournamentIdAndJudgeIdAndPlace(
                                            categoryId,
                                            judgeId,
                                            place
                                    );
                } else {
                    duplicate =
                            finalMarkRepository
                                    .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlace(
                                            categoryId,
                                            judgeId,
                                            danceId,
                                            place
                                    );
                }
            } else {
                if (danceId == 0L) {
                    duplicate =
                            finalMarkRepository
                                    .existsByCategoryTournamentIdAndJudgeIdAndPlaceAndIdNot(
                                            categoryId,
                                            judgeId,
                                            place,
                                            currentFinalMarkId
                                    );
                } else {
                    duplicate =
                            finalMarkRepository
                                    .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                                            categoryId,
                                            judgeId,
                                            danceId,
                                            place,
                                            currentFinalMarkId
                                    );
                }
            }
        }

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Judge has already assigned place " + place
            );
        }
    }


    @Override
    public List<FinalMarkDto> getAll() {

        log.debug("Loading all final marks");

        List<FinalMarkDto> result = finalMarkRepository.findAll()
                .stream()
                .map(FinalMarkDto::fromEntity)
                .toList();

        log.info("Loaded {} final marks", result.size());


        return result;
    }

    @Override
    public List<FinalMarkDto> getByCategory(Long categoryId) {
        log.debug(
                "Loading final marks for categoryId={}",
                categoryId
        );

        List<FinalMarkDto> result = finalMarkRepository.findByCategoryTournamentId(categoryId)
                .stream()
                .map(FinalMarkDto::fromEntity)
                .toList();

        log.info(
                "Loaded {} final marks for categoryId={}",
                result.size(),
                categoryId
        );

        return result;
    }

    @Override
    public FinalMarkDto create(FinalMarkRequestDto dto) {

        log.info(
                "Creating final mark: participantId={}, categoryId={}, judgeId={}, danceId={}, place={}",
                dto.getParticipantId(),
                dto.getCategoryTournamentId(),
                dto.getJudgeId(),
                dto.getDanceId(),
                dto.getPlace()
        );


        FinalMark finalMark = dto.toEntity();
        finalMark.setParticipantTournament(participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Participant not found")));

        finalMark.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("Category not found")));

        finalMark.setJudge(judgeRepository.findById(dto.getJudgeId())
                .orElseThrow(() -> new NotFoundException("Judge not found")));

        finalMark.setPlace(dto.getPlace());

        if (dto.getDanceId() != null){

            finalMark.setDance(danceRepository.findById(dto.getDanceId())
                    .orElseThrow(() -> new NotFoundException("Dance not found")));

        }

        validatePlace(
                finalMark.getCategoryTournament().getId(),
                finalMark.getJudge().getId(),
                finalMark.getDance().getId(),
                finalMark.getPlace(),
                null

        );

        FinalMark saved = finalMarkRepository.save(finalMark);

        log.info(
                "Final mark created successfully: id={}, participantId={}, categoryId={}, judgeId={}, place={}",
                saved.getId(),
                dto.getParticipantId(),
                dto.getCategoryTournamentId(),
                dto.getJudgeId(),
                saved.getPlace()
        );

        return FinalMarkDto.fromEntity(saved);
    }

    @Override
    public void delete(Long id) {

        log.info(
                "Deleting final mark: id={}",
                id
        );

        finalMarkRepository.deleteById(id);

        log.info(
                "Final mark deleted successfully: id={}",
                id
        );

    }

    @Override
    public FinalMarkDto patchUpdate(Long id, FinalMarkPatchDto dto) {

        log.info(
                "Updating final mark: id={}, participantId={}, categoryId={}, judgeId={}, place={}",
                id,
                dto.getParticipantId(),
                dto.getCategoryTournamentId(),
                dto.getJudgeId(),
                dto.getPlace()
        );

        FinalMark existing = finalMarkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Final mark with id " + id + " not found"));

        if (dto.getParticipantId() != null) {

            log.debug(
                    "Changing participant: finalMarkId={}, participantId={}",
                    id,
                    dto.getParticipantId()
            );

            existing.setParticipantTournament(participantRepository.findById(dto.getParticipantId())
                    .orElseThrow(() -> new NotFoundException("Participant tournament with id " + dto.getParticipantId() + " not found")));
        }

        if (dto.getCategoryTournamentId() != null) {

            log.debug(
                    "Changing category: finalMarkId={}, categoryId={}",
                    id,
                    dto.getCategoryTournamentId()
            );


            existing.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                    .orElseThrow(() -> new NotFoundException("Category tournament with id " +
                            dto.getCategoryTournamentId() + " not found")));
        }

        if (dto.getJudgeId() != null) {

            log.debug(
                    "Changing judge: finalMarkId={}, judgeId={}",
                    id,
                    dto.getJudgeId()
            );

            existing.setJudge(judgeRepository.findById(dto.getJudgeId())
                    .orElseThrow(() -> new NotFoundException("Judge with id " + dto.getJudgeId() + " not found")));
        }

        if (existing.isLocked()) {
            throw new IllegalStateException("Round is locked");
        }

        if (dto.getDanceId() != null) {
            log.debug(
                    "Changing judge: finalMarkId={}, judgeId={}",
                    id,
                    dto.getJudgeId()
            );

            existing.setDance(danceRepository.findById(dto.getDanceId())
                    .orElseThrow(() -> new NotFoundException("Dance with id " + dto.getDanceId() + " not found")));
        }

        Integer newPlace = Optional.ofNullable(dto.getPlace())
                .orElse(0);

        validatePlace(
                existing.getCategoryTournament().getId(),
                existing.getJudge().getId(),
                existing.getDance().getId(),
                newPlace,
                existing.getId()

        );


        Integer oldPlace = existing.getPlace();


        if (!Objects.equals(oldPlace, newPlace)) {

            log.info(
                    "Changing place: finalMarkId={}, oldPlace={}, newPlace={}",
                    id,
                    oldPlace,
                    newPlace
            );

            existing.setPlace(newPlace);
        }


        FinalMark updated = finalMarkRepository.save(existing);

        log.info(
                "Final mark updated successfully: id={}, place={}",
                updated.getId(),
                updated.getPlace()
        );

        return FinalMarkDto.fromEntity(updated);
    }

    @Override
    public FinalMarkDto updatePlace(Long id, FinalMarkPatchJudgeDto dto) {

        log.info(
                "Judge updating place: finalMarkId={}, newPlace={}",
                id,
                dto.getPlace()
        );


        FinalMark existing = finalMarkRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "FinalMark with id " + id + " not found"
                        )
                );

        if (existing.isLocked()) {
            throw new IllegalStateException("Round is locked");
        }

        int newPlace = dto.getPlace();

        validatePlace(
                existing.getCategoryTournament().getId(),
                existing.getJudge().getId(),
                existing.getDance().getId(),
                newPlace,
                existing.getId()

        );

        int oldPlace = existing.getPlace();

        existing.setPlace(newPlace);

        FinalMark updated = finalMarkRepository.save(existing);

        log.info(
                "Judge changed place successfully: finalMarkId={}, oldPlace={}, newPlace={}",
                id,
                oldPlace,
                newPlace
        );

        return FinalMarkDto.fromEntity(updated);

    }

    @Override
    public List<FinalMarkDto> getFinalMarksForJudgeName(String judgeName) {

        log.debug(
                "Loading final marks for judge: {}",
                judgeName
        );

        List<FinalMarkDto> result = finalMarkRepository.findByJudge_NameJudge(judgeName).stream()
                .map(FinalMarkDto::fromEntity)
                .collect(Collectors.toList());

        log.info(
                "Loaded {} final marks for judge={}",
                result.size(),
                judgeName
        );

        return result;
    }

    public void validateSingleDanceFinalMarks(List<FinalMark> marks) {

        Set<Long> danceIds = new HashSet<>();
        boolean hasNullDance = false;

        for (FinalMark mark : marks) {
            if (mark.getDance() == null) {
                hasNullDance = true;
            } else {
                danceIds.add(mark.getDance().getId());
            }
        }

        if (danceIds.size() > 1 || (hasNullDance && !danceIds.isEmpty())) {
            throw new IllegalArgumentException(
                    "computeSkating expects marks for one dance only"
            );
        }

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                marks.stream()
                        .collect(Collectors.groupingBy(
                                FinalMark::getParticipantTournament,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        int judgesCount = marksByParticipant.values()
                .stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        int participantsCount = marks.stream()
                .map(FinalMark::getParticipantTournament)
                .map(ParticipantTournament::getId)
                .distinct()
                .toList()
                .size();

        validateFinalMarks(marksByParticipant, judgesCount, participantsCount);
    }

    public void validateFinalMarks(Map<ParticipantTournament, List<FinalMark>> marksByParticipant, int judgesCount,
                                   int participantCount) {

        if (judgesCount == 0) {
            throw new IllegalStateException(
                    "No judge marks found"
            );
        }

        Set<Long> judgeIds = new HashSet<>();

        for (Map.Entry<ParticipantTournament, List<FinalMark>> entry
                : marksByParticipant.entrySet()) {

            List<FinalMark> participantMarks = entry.getValue();

            if (participantMarks.size() != judgesCount) {
                throw new IllegalStateException(
                        "Participant " +
                                entry.getKey().getId() +
                                " does not have marks from all judges"
                );
            }

            judgeIds.clear();

            for (FinalMark mark : participantMarks) {

                if (mark.getJudge() == null ||
                        mark.getJudge().getId() == null) {

                    throw new IllegalStateException(
                            "Judge is missing for final mark " +
                                    mark.getId()
                    );
                }

                if (!judgeIds.add(mark.getJudge().getId())) {
                    throw new IllegalStateException(
                            "Duplicate judge mark for participant " +
                                    entry.getKey().getId()
                    );
                }

                int place = mark.getPlace();

                if (place < 1 || place > participantCount) {
                    throw new IllegalStateException(
                            "Invalid place " +
                                    place +
                                    " for participant " +
                                    entry.getKey().getId()
                    );
                }
            }
        }
    }
}
