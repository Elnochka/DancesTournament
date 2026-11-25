package com.dances.service.impl;

import com.dances.entity.*;
import com.dances.entity.dto.*;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.*;
import com.dances.service.MarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkServiceImpl implements MarkService {

    private final MarkRepository markRepository;
    private final ParticipantTournamentRepository participantRepository;
    private final CategoryTournamentRepository categoryRepository;
    private final JudgeRepository judgeRepository;
    private final FinalMarkRepository finalMarkRepository;
    private final DanceRepository danceRepository;


    @Override
    public List<MarkDto> getAll() {

        List<MarkDto> result =  markRepository.findAll().stream()
                .map(MarkDto::fromEntity)
                .toList();

        log.info("Got {} marks successfully", result.size());

        return result;

    }

    @Override
    public MarkDto create(MarkRequestDto dto) {
        Mark mark = new Mark();

        mark.setParticipantTournament(participantRepository.findById(dto.getParticipantTournamentId())
                .orElseThrow(() -> new NotFoundException("Participant not found")));

        mark.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("Category not found")));

        mark.setJudge(judgeRepository.findById(dto.getJudgeId())
                .orElseThrow(() -> new NotFoundException("Judge not found")));

        mark.setRound(dto.getRound());
        mark.setPassed(dto.isPassed());
        mark.setLocked(false);

        Mark saved = markRepository.save(mark);

        log.info(
                "Mark created: id={}, participant={}, category={}, judge={}, round={}, passed={}",
                saved.getId(),
                dto.getParticipantTournamentId(),
                dto.getCategoryTournamentId(),
                dto.getJudgeId(),
                dto.getRound(),
                dto.isPassed()
        );

        return MarkDto.fromEntity(saved);
    }

    @Override
    public List<MarkDto> getMarks(Long categoryId, RoundEnum round) {

        List<MarkDto> result = markRepository.findByCategoryTournamentIdAndRound(categoryId, round)
                .stream()
                .map(MarkDto::fromEntity)
                .collect(Collectors.toList());

        log.info("Got {} marks successfully", result.size());

        return result;
    }

    @Override
    public MarkDto patchUpdate(Long id, MarkPatchDto dto) {
        Mark existing = markRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mark with id " + id + " not found"));

        if (existing.isLocked()) {
            throw new IllegalStateException("Round is locked");
        }

        boolean oldValue = existing.isPassed();
        boolean newValue = dto.isPassed();

        if (oldValue != newValue) {
            existing.setPassed(dto.isPassed());
        }

        if (dto.getRound() != null) {
            existing.setRound(dto.getRound());
        }

        if (dto.getParticipantTournamentId() != null) {
            existing.setParticipantTournament(participantRepository.findById(dto.getParticipantTournamentId())
                    .orElseThrow(() -> new NotFoundException("Participant with id " + dto.getParticipantTournamentId() + " not found")));
        }

        if (dto.getJudgeId() != null) {

            existing.setJudge(judgeRepository.findById(dto.getJudgeId())
                    .orElseThrow(() -> new NotFoundException("Judge with id " + dto.getJudgeId() + " not found")));
        }

        if (dto.getCategoryTournamentId() != null) {

            existing.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                    .orElseThrow(() -> new NotFoundException("Category tournament with id " + dto.getCategoryTournamentId() + " not found")));
        }

        if (dto.getDanceId() != null) {

            existing.setDance(danceRepository.findById(dto.getDanceId())
                    .orElseThrow(() -> new NotFoundException("Dance with id " + dto.getDanceId() + " not found")));
        }

        Mark updated = markRepository.save(existing);

        log.info(
                "Judge updated mark: id={}, passed {} -> {}",
                id,
                oldValue,
                newValue
        );

        return MarkDto.fromEntity(updated);
    }

    @Override
    public List<MarkDto> getMarksForJudgeName(String judgeName) {

        return markRepository.findByJudge_NameJudge(judgeName).stream()
                .map(MarkDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MarkDto updatePassed(Long id, MarkPatchJudgeDto dto) {

        Mark existing = markRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mark with id " + id + " not found"));

        if (existing.isLocked()) {
            throw new IllegalStateException("Round is locked");
        }

        boolean oldValue = existing.isPassed();
        boolean newValue = dto.isPassed();

        if (oldValue != newValue) {
            existing.setPassed(dto.isPassed());
        }

        Mark updated = markRepository.save(existing);
        return MarkDto.fromEntity(updated);
    }

    @Override
    public void validateNonSocialMarks(List<Mark> marks) {

        Set<String> uniqueMarks = new HashSet<>();

        for (Mark mark : marks) {

            if (mark.getDance() == null) {
                throw new IllegalStateException(
                        "Dance is required for non-social mark: "
                                + mark.getId()
                );
            }

            String key =
                    mark.getParticipantTournament().getId()
                            + "-"
                            + mark.getJudge().getId()
                            + "-"
                            + mark.getDance().getId();

            if (!uniqueMarks.add(key)) {
                throw new IllegalStateException(
                        "Duplicate mark for participant="
                                + mark.getParticipantTournament().getId()
                                + ", judge="
                                + mark.getJudge().getId()
                                + ", dance="
                                + mark.getDance().getId()
                );
            }
        }
    }

    @Override
    @Transactional
    public void createMarks(CategoryTournament category, RoundEnum nextRound,
                            List<ParticipantTournamentDto> participants, List<Judge> judges, boolean social) {

        List<Dance> dances = social
                ? Collections.emptyList()
                : danceRepository.findByKindOfDanceId(
                category.getKindOfDance().getId()
        );

        for (ParticipantTournamentDto participantDto : participants) {
            ParticipantTournament participant =
                    participantRepository.findById(participantDto.getId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "ParticipantTournament not found: "
                                                    + participantDto.getId()
                                    )
                            );

            for (Judge judge : judges) {
                if (nextRound == RoundEnum.FINAL) {

                    createFinalMarks(category, participant, judge, social, dances);

                } else {

                    createRoundMarks(category, participant, judge, nextRound, social, dances);

                }
            }
        }
    }

    private void createRoundMarks(CategoryTournament category, ParticipantTournament participant,
                                    Judge judge, RoundEnum round, boolean social, List<Dance> dances) {

        if (social) {

            Mark mark = new Mark();
            mark.setJudge(judge);
            mark.setParticipantTournament(participant);
            mark.setCategoryTournament(category);
            mark.setRound(round);
            mark.setPassed(false);
            mark.setLocked(false);

            markRepository.save(mark);
            return;
        }

        for (Dance dance : dances) {

            Mark mark = new Mark();
            mark.setJudge(judge);
            mark.setParticipantTournament(participant);
            mark.setCategoryTournament(category);
            mark.setRound(round);
            mark.setDance(dance);
            mark.setPassed(false);
            mark.setLocked(false);

            markRepository.save(mark);
        }
    }

    private void createFinalMarks(CategoryTournament category, ParticipantTournament participant,
                                    Judge judge, boolean social, List<Dance> dances) {

        if (social) {

            FinalMark mark = new FinalMark();
            mark.setJudge(judge);
            mark.setParticipantTournament(participant);
            mark.setCategoryTournament(category);
            mark.setPlace(0);
            mark.setLocked(false);

            finalMarkRepository.save(mark);
            return;

        }

        for (Dance dance : dances) {

            FinalMark mark = new FinalMark();
            mark.setJudge(judge);
            mark.setParticipantTournament(participant);
            mark.setCategoryTournament(category);
            mark.setDance(dance);
            mark.setPlace(0);
            mark.setLocked(false);

            finalMarkRepository.save(mark);

        }
    }

}
