package com.dances.service.impl;

import com.dances.entity.CategoryTournament;
import com.dances.entity.RoundSettings;
import com.dances.entity.dto.RoundSettingsDto;
import com.dances.entity.dto.RoundSettingsRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.ParticipantTournamentRepository;
import com.dances.repository.RoundSettingsRepository;
import com.dances.service.RoundSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoundSettingsServiceImpl implements RoundSettingsService {

    private final RoundSettingsRepository roundSettingsRepository;
    private final CategoryTournamentRepository categoryRepository;
    private final ParticipantTournamentRepository participantRepository;

    @Transactional(readOnly = true)
    public RoundSettingsDto getSettings(
            Long categoryId,
            RoundEnum round
    ) {

        CategoryTournament category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CategoryTournament not found: " + categoryId
                        )
                );

        int participantsCount = (int) participantRepository.countByCategoryTournamentId(categoryId);

        Optional<RoundSettings> existing = roundSettingsRepository.findByCategoryTournamentIdAndRound(
                categoryId,
                round
        );

        int recommended = participantsCount;

        if (round != RoundEnum.DEFAULT) {
            recommended = calculateRecommended(participantsCount, round);
        }


        if (existing.isEmpty()) {
            return RoundSettingsDto.builder()
                    .categoryTournamentId(categoryId)
                    .round(round)
//                    .participantsCount(participantsCount)
//                    .recommendedCouplesToNextRound(recommended)
                    .couplesToNextRound(recommended)
                    .build();
        }

        RoundSettings settings = existing.get();

        return RoundSettingsDto.builder()
                .id(settings.getId())
                .categoryTournamentId(categoryId)
                .round(round)
//                .participantsCount(participantsCount)
//                .recommendedCouplesToNextRound(recommended)
                .couplesToNextRound(
                        settings.getCouplesToNextRound()
                )
                .build();
    }

    public RoundSettingsDto save(
            Long categoryId,
            RoundEnum round,
            Integer couplesToNextRound
    ) {

        if (round == RoundEnum.FINAL) {
            throw new IllegalArgumentException(
                    "FINAL has no next round"
            );
        }

        CategoryTournament category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CategoryTournament not found: " + categoryId
                        )
                );

        int participantsCount = (int) participantRepository.countByCategoryTournamentId(categoryId);

        validate(couplesToNextRound, participantsCount);

        RoundSettings settings = roundSettingsRepository
                .findByCategoryTournamentIdAndRound(
                        categoryId,
                        round
                )
                .orElseGet(() ->
                        RoundSettings.builder()
                                .categoryTournament(category)
                                .round(round)
                                .build()
                );

        settings.setCouplesToNextRound(couplesToNextRound);

        RoundSettings saved = roundSettingsRepository.save(settings);

        log.info(
                "Round settings saved: category={}, round={}, participants={}, couplesToNextRound={}",
                categoryId,
                round,
                participantsCount,
                couplesToNextRound
        );

        return getSettings(categoryId, round);
    }

    public RoundEnum resolveNextRound(Long categoryId, RoundSettings currentSettings) {

        int nextRoundOrder = currentSettings.getRoundOrder() + 1;

        return roundSettingsRepository
                .findByCategoryTournamentId(categoryId)
                .stream()
                .filter(settings ->
                        settings.getRoundOrder() == nextRoundOrder)
                .map(RoundSettings::getRound)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Next round not found for category: "
                                        + categoryId
                                        + ", current round: "
                                        + currentSettings.getRound()
                        ));
    }

    @Transactional(readOnly = true)
    public int getCouplesToNextRound(Long categoryId, RoundEnum round) {

        RoundSettings settings = roundSettingsRepository
                .findByCategoryTournamentIdAndRound(
                        categoryId,
                        round
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Round settings not configured for category "
                                        + categoryId
                                        + ", round "
                                        + round
                        )
                );

        if (settings.getCouplesToNextRound() == null) {
            throw new IllegalStateException(
                    "couplesToNextRound is not configured"
            );
        }

        return settings.getCouplesToNextRound();
    }

    private int calculateRecommended(int participantsCount, RoundEnum round) {

        if (round == RoundEnum.FINAL) {
            return 0;
        }

        /*
         * Тимчасово не прив'язуємо це до конкретної
         * формули WDSF.
         *
         * Тут пізніше зробимо правила WDSF.
         */
        return Math.max(
                1,
                participantsCount / 2
        );
    }

    private void validate(Integer couplesToNextRound, int participantsCount) {

        if (couplesToNextRound == null) {
            throw new IllegalArgumentException(
                    "couplesToNextRound must not be null"
            );
        }

        if (couplesToNextRound < 1) {
            throw new IllegalArgumentException(
                    "couplesToNextRound must be greater than 0"
            );
        }

        if (couplesToNextRound > participantsCount) {
            throw new IllegalArgumentException(
                    "couplesToNextRound must be less than participants count"
            );
        }
    }

    @Override
    public RoundSettingsDto createRoundSettings(RoundSettingsRequestDto dto) {

        RoundSettings p = dto.toEntity();

        p.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("CategoryTournament not found")));


        RoundSettings saved = roundSettingsRepository.save(p);
        return RoundSettingsDto.fromEntity(saved);
    }

    @Override
    public List<RoundSettingsDto> getAll() {
        return roundSettingsRepository.findAll().stream()
                .map(RoundSettingsDto::fromEntity)
                .toList();
    }

    @Override
    public List<RoundSettingsDto> getRounds(Long categoryId) {
        return roundSettingsRepository.findByCategoryTournamentId(categoryId).stream()
                .map(RoundSettingsDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<RoundSettingsDto> getById(Long id) {
        return roundSettingsRepository.findById(id).map(RoundSettingsDto::fromEntity);
    }

    @Override
    public void deleteById(Long id) {
        roundSettingsRepository.deleteById(id);
    }

    @Override
    public RoundSettingsDto patchUpdate(Long id, RoundSettingsDto dto) {
        RoundSettings existing = roundSettingsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RoundSettings with id " + id + " not found"));

        if (dto.getCouplesToNextRound() != null) existing.setCouplesToNextRound(dto.getCouplesToNextRound());

        if (dto.getRound() != null) existing.setRound(dto.getRound());
        if (dto.getCategoryTournamentId() != null) {

            existing.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                    .orElseThrow(() -> new NotFoundException("Klass with id " + dto.getCategoryTournamentId() + " not found")));
        }
        if (dto.getRoundOrder() != null) existing.setRoundOrder(dto.getRoundOrder());

        RoundSettings updated = roundSettingsRepository.save(existing);
        return RoundSettingsDto.fromEntity(updated);
    }
}
