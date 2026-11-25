package com.dances.service.impl;

import com.dances.entity.*;
import com.dances.entity.dto.ChangeCoupleRequest;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.HistoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final ParticipantRepository participantRepository;
    private final TrainerRepository trainerRepository;
    private final ParticipantTrainerHistoryRepository participantTrainerHistoryRepository;

    private final CoupleRepository coupleRepository;
    private final CoupleHistoryRepository coupleHistoryRepository;

    private final ClubRepository clubRepository;
    private final TrainerClubHistoryRepository trainerClubHistoryRepository;

    @Transactional
    @Override
    public ParticipantTrainerHistory changeTrainer(Long participantId, Long newTrainerId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Participant not found: " + participantId));
        Trainer trainer = trainerRepository.findById(newTrainerId)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + newTrainerId));

        // 1) close old active history
        participantTrainerHistoryRepository.closeActiveByParticipantId(participantId, LocalDate.now());

        // 2) create new record
        ParticipantTrainerHistory participantTrainerHistory = ParticipantTrainerHistory.builder()
                .participant(participant)
                .trainer(trainer)
                .startDate(LocalDate.now())
                .build();
        participantTrainerHistoryRepository.save(participantTrainerHistory);

        // 3) update participant current trainer
        participant.setTrainer(trainer);
        participantRepository.save(participant);

        return participantTrainerHistory;
    }

    @Transactional
    @Override
    public CoupleHistory changeCoupleMembers(Long coupleId, ChangeCoupleRequest dto) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new NotFoundException("Couple not found: " + coupleId));
        Participant male;
        Participant female;

        if(dto.getMaleId() != null) {
            male = participantRepository.findById(dto.getMaleId())
                    .orElseThrow(() -> new NotFoundException("Participant not found: " + dto.getMaleId()));
        } else {
            male = couple.getParticipantFemale();
        }

        if (dto.getFemaleId() != null) {
            female = participantRepository.findById(dto.getFemaleId())
                    .orElseThrow(() -> new NotFoundException("Participant not found: " + dto.getFemaleId()));
        } else {
            female = couple.getParticipantMale();
        }

        // close previous couple history
        coupleHistoryRepository.closeActiveByCoupleId(coupleId, LocalDate.now());

        // create new history
        CoupleHistory coupleHistory = CoupleHistory.builder()
                .couple(couple)
                .maleParticipant(male)
                .femaleParticipant(female)
                .startDate(LocalDate.now())
                .build();
        coupleHistoryRepository.save(coupleHistory);

        // update couple
        if (dto.getMaleId() != null) {
            couple.setParticipantMale(male);
        }
        if (dto.getFemaleId() != null) {
            couple.setParticipantFemale(female);
        }
        coupleRepository.save(couple);

        return coupleHistory;
    }

    @Transactional
    @Override
    public TrainerClubHistory changeClub(Long trainerId, Long newClubId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + trainerId));
        Club club = clubRepository.findById(newClubId)
                .orElseThrow(() -> new NotFoundException("Club not found: " + newClubId));

        // 1) close old active history
        trainerClubHistoryRepository.closeActiveByTrainerId(trainerId, LocalDate.now());

        // 2) create new record
        TrainerClubHistory trainerClubHistory = TrainerClubHistory.builder()
                .trainer(trainer)
                .club(club)
                .startDate(LocalDate.now())
                .build();
        trainerClubHistoryRepository.save(trainerClubHistory);

        // 3) update participant current trainer
        trainer.setClub(club);
        trainerRepository.save(trainer);

        return trainerClubHistory;
    }
}
