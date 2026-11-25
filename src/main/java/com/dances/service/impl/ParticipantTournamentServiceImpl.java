package com.dances.service.impl;

import com.dances.entity.*;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.entity.dto.ParticipantTournamentPatchDto;
import com.dances.entity.dto.ParticipantTournamentRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.model.SoloCoupleEnum;
import com.dances.repository.*;
import com.dances.service.ParticipantTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantTournamentServiceImpl implements ParticipantTournamentService {

    private final ParticipantTournamentRepository participantTournamentRepository;
    private final ParticipantRepository participantRepository;
    private final CoupleRepository coupleRepository;
    private final CategoryTournamentRepository categoryTournamentRepository;
    private final MarkRepository markRepository;
    private final FinalMarkRepository finalMarkRepository;
    private final ParticipantPassingService participantPassingService;
    private final ParticipantTournamentValidator participantTournamentValidator;


    @Override
    public List<ParticipantTournamentDto> getAll() {
        return participantTournamentRepository.findAll().stream()
                .map(ParticipantTournamentDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<ParticipantTournamentDto> getById(Long id) {
        return participantTournamentRepository.findById(id).map(ParticipantTournamentDto::fromEntity);
    }

    @Override
    public List<ParticipantTournamentDto> getByCategory(Long categoryId) {
        return participantTournamentRepository.findByCategoryTournamentId(categoryId)
                .stream()
                .map(ParticipantTournamentDto::fromEntity)
                .toList();
    }

    @Override
    public List<ParticipantTournamentDto> getByCategoryAndType(Long categoryId, SoloCoupleEnum type) {
        return participantTournamentRepository.findByCategoryTournamentIdAndSoloCouple(categoryId, type)
                .stream().map(ParticipantTournamentDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public ParticipantTournamentDto create(ParticipantTournamentRequestDto dto) {

        CategoryTournament category = categoryTournamentRepository
                .findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (dto.getSoloCouple() == null) {
            throw new IllegalArgumentException("SoloCouple is required");
        }

        participantTournamentValidator.validateSoloCouple(
                dto.getSoloCouple(), category
        );

        Participant participant = null;
        Couple couple = null;

        if (dto.getSoloCouple() == SoloCoupleEnum.SOLO) {
            participant = participantRepository.findById(dto.getParticipantId())
                    .orElseThrow(() -> new NotFoundException("Participant not found"));

            participantTournamentValidator.validateParticipantMatchesCategory(
                    participant, category
            );

        } else if (dto.getSoloCouple() == SoloCoupleEnum.COUPLE) {
            couple = coupleRepository.findById(dto.getCoupleId())
                    .orElseThrow(() -> new NotFoundException("Couple not found"));

            participantTournamentValidator.validateCoupleMatchesCategory(
                    couple, category
            );

        }

        validateNoDuplicate(category, dto.getSoloCouple(), participant, couple, null);

        int nextStartNumber = participantTournamentRepository
                .findLastStartNumber(category.getId(), dto.getSoloCouple()) + 1;

        ParticipantTournament pt = ParticipantTournament.builder()
                .startNumber(nextStartNumber)
                .participant(participant)
                .couple(couple)
                .categoryTournament(category)
                .soloCouple(dto.getSoloCouple())
                .build();

        return ParticipantTournamentDto.fromEntity(
                participantTournamentRepository.save(pt)
        );
    }

    @Override
    @Transactional
    public ParticipantTournamentDto patchUpdate(Long id, ParticipantTournamentPatchDto dto) {

        ParticipantTournament existing = participantTournamentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "ParticipantTournament not found"
                ));

        CategoryTournament category = resolveCategoryTournament(existing, dto);

        if (hasRegistrationChanged(existing, dto)) {
            validateCanChangeRegistration(
                    existing.getCategoryTournament().getId(),
                    existing.getId()
            );
        }

        SoloCoupleEnum soloCoupleEnum = dto.getSoloCouple() != null
                ? dto.getSoloCouple()
                : existing.getSoloCouple();

        participantTournamentValidator.validateSoloCouple(
                soloCoupleEnum, category
        );

        Participant participant = resolveParticipant(existing, dto);
        Couple couple = resolveCouple(existing, dto);

        if (soloCoupleEnum == SoloCoupleEnum.SOLO) {
            if (participant == null) {
                throw new IllegalArgumentException("Participant is required");
            }

            participantTournamentValidator.validateParticipantMatchesCategory(participant, category);

            couple = null;
        } else {
            if (couple == null) {
                throw new IllegalArgumentException("Couple is required");
            }

            participantTournamentValidator.validateCoupleMatchesCategory(couple, category);

            participant = null;
        }

        validateNoDuplicate(category, soloCoupleEnum, participant, couple, existing.getId());

        existing.setSoloCouple(soloCoupleEnum);
        existing.setCategoryTournament(category);
        existing.setParticipant(participant);
        existing.setCouple(couple);

        return ParticipantTournamentDto.fromEntity(
                participantTournamentRepository.save(existing)
        );
    }

    private CategoryTournament resolveCategoryTournament(
            ParticipantTournament existing,
            ParticipantTournamentPatchDto dto
    ) {
        if (dto.getCategoryTournamentId() == null) {
            return existing.getCategoryTournament();
        }

        return categoryTournamentRepository.findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private Participant resolveParticipant(
            ParticipantTournament existing,
            ParticipantTournamentPatchDto dto
    ) {
        if (dto.getParticipantId() == null) {
            return existing.getParticipant();
        }

        return participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Participant not found"));
    }

    private Couple resolveCouple(
            ParticipantTournament existing,
            ParticipantTournamentPatchDto dto
    ) {
        if (dto.getCoupleId() == null) {
            return existing.getCouple();
        }

        return coupleRepository.findById(dto.getCoupleId())
                .orElseThrow(() -> new NotFoundException("Couple not found"));
    }

    private boolean hasRegistrationChanged(
            ParticipantTournament existing,
            ParticipantTournamentPatchDto dto
    ) {
        Long currentCategoryId = existing.getCategoryTournament().getId();
        Long currentParticipantId = existing.getParticipant() != null
                ? existing.getParticipant().getId()
                : null;
        Long currentCoupleId = existing.getCouple() != null
                ? existing.getCouple().getId()
                : null;

        return (dto.getCategoryTournamentId() != null
                && !Objects.equals(dto.getCategoryTournamentId(), currentCategoryId))
                || (dto.getParticipantId() != null
                && !Objects.equals(dto.getParticipantId(), currentParticipantId))
                || (dto.getCoupleId() != null
                && !Objects.equals(dto.getCoupleId(), currentCoupleId));
    }

    private void validateCanChangeRegistration(Long categoryId, Long id) {

        boolean hasMarks = markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(categoryId, id);

        boolean hasFinalMarks = finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(categoryId, id);

        if (hasMarks || hasFinalMarks) {
            throw new IllegalStateException(
                    "Cannot change participant: marks already exist"
            );
        }

    }

    private void validateNoDuplicate(
            CategoryTournament category,
            SoloCoupleEnum type,
            Participant participant,
            Couple couple,
            Long excludedId
    ) {
        boolean duplicate;

        if (type == SoloCoupleEnum.SOLO) {
            duplicate = excludedId == null
                    ? participantTournamentRepository
                    .existsByCategoryTournamentIdAndParticipantId(
                            category.getId(), participant.getId())
                    : participantTournamentRepository
                    .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                            category.getId(), participant.getId(), excludedId);

            if (duplicate) {
                throw new IllegalArgumentException(
                        "Participant already registered in this category"
                );
            }
        } else {
            duplicate = excludedId == null
                    ? participantTournamentRepository
                    .existsByCategoryTournamentIdAndCoupleId(
                            category.getId(), couple.getId())
                    : participantTournamentRepository
                    .existsByCategoryTournamentIdAndCoupleIdAndIdNot(
                            category.getId(), couple.getId(), excludedId);

            if (duplicate) {
                throw new IllegalArgumentException(
                        "Couple already registered in this category"
                );
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        participantTournamentRepository.deleteById(id);
    }

    @Override
    public List<ParticipantTournamentDto> selectParticipants(CategoryTournament category, RoundSettings settings,
                                                             boolean social) {

        RoundEnum round = settings.getRound();

        if (round == RoundEnum.DEFAULT) {
            return participantTournamentRepository
                    .findByCategoryTournamentId(category.getId())
                    .stream()
                    .map(ParticipantTournamentDto::fromEntity)
                    .toList();
        }

        if (social) {
            markRepository.lockRound(category.getId(), round);
        }

        return participantPassingService.calculatePassing(category.getId(), round, settings.getCouplesToNextRound());
    }

}
