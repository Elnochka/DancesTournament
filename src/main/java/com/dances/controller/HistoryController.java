package com.dances.controller;

import com.dances.entity.CoupleHistory;
import com.dances.entity.ParticipantTrainerHistory;
import com.dances.entity.TrainerClubHistory;
import com.dances.entity.dto.*;
import com.dances.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/participants/{participantId}/change-trainer")
    public ResponseEntity<ParticipantTrainerHistoryDto> changeTrainer(
            @PathVariable Long participantId,
            @RequestBody ChangeTrainerRequest req) {

        ParticipantTrainerHistory h = historyService.changeTrainer(participantId, req.getTrainerId());
        return ResponseEntity.ok(ParticipantTrainerHistoryDto.fromEntity(h));
    }

    @PostMapping("/couples/{coupleId}/change-members")
    public ResponseEntity<CoupleHistoryDto> changeCouple(
            @PathVariable Long coupleId,
            @RequestBody ChangeCoupleRequest req) {

        CoupleHistory h = historyService.changeCoupleMembers(coupleId, req);
        return ResponseEntity.ok(CoupleHistoryDto.fromEntity(h));
    }

    @PostMapping("/trainers/{trainerId}/change-club")
    public ResponseEntity<TrainerClubHistoryDto> changeClub(
            @PathVariable Long trainerId,
            @RequestBody ChangeClubRequest req) {

        TrainerClubHistory h = historyService.changeClub(trainerId, req.getClubId());
        return ResponseEntity.ok(TrainerClubHistoryDto.fromEntity(h));
    }
}
