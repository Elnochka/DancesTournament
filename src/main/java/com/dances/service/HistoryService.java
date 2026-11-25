package com.dances.service;

import com.dances.entity.CoupleHistory;
import com.dances.entity.ParticipantTrainerHistory;
import com.dances.entity.TrainerClubHistory;
import com.dances.entity.dto.ChangeCoupleRequest;

public interface HistoryService {

    ParticipantTrainerHistory changeTrainer(Long participantId, Long newTrainerId);

    CoupleHistory changeCoupleMembers(Long coupleId, ChangeCoupleRequest req);

    TrainerClubHistory changeClub(Long trainerId, Long newClubId);
}
