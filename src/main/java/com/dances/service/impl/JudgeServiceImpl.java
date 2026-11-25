package com.dances.service.impl;

import com.dances.entity.Judge;
import com.dances.entity.dto.JudgeDto;
import com.dances.entity.dto.JudgePatchDto;
import com.dances.entity.dto.JudgeRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.JudgeRepository;
import com.dances.repository.TrainerRepository;
import com.dances.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeRepository judgeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public List<JudgeDto> getAll() {
        return judgeRepository.findAll().stream()
                .map(JudgeDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<JudgeDto> getById(Long id) {
        return judgeRepository.findById(id).map(JudgeDto::fromEntity);
    }

    @Override
    public JudgeDto create(JudgeRequestDto dto) {
        Judge p = dto.toEntity();
        p.setTrainer(trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new NotFoundException("Kind of dance not found")));

        Judge saved = judgeRepository.save(p);
        return JudgeDto.fromEntity(saved);
    }

    @Override
    public JudgeDto patchUpdate(Long id, JudgePatchDto dto) {
        Judge existing = judgeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Judge with id " + id + " not found"));

        if (dto.getNameJudge() != null) existing.setNameJudge(dto.getNameJudge());

        if (dto.getComment() != null) existing.setComment(dto.getComment());

        if (dto.getTrainerId() != null) {
            existing.setTrainer(trainerRepository.findById(dto.getTrainerId())
                    .orElseThrow(() -> new NotFoundException("Kind of dance with id " +
                            dto.getTrainerId() + " not found")));
        }

        Judge updated = judgeRepository.save(existing);
        return JudgeDto.fromEntity(updated);
    }

    @Override
    public void deleteById(Long id) {
        judgeRepository.deleteById(id);
    }
}
