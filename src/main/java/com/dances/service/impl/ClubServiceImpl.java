package com.dances.service.impl;

import com.dances.entity.Club;
import com.dances.exception.DuplicateException;
import com.dances.exception.NotFoundException;
import com.dances.repository.ClubRepository;
import com.dances.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;

    @Override
    public Club createClub(Club club) {
        if (clubRepository.existsByNameClub(club.getNameClub())) {
            validateUniqueName(club.getNameClub());
        }
        return clubRepository.save(club);
    }

    @Override
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Override
    public Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);
    }

//    @Override
//    public Club updateClub(Long id, Club updatedClub) {
//        Club club = clubRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Club not found"));
//        club.setNameClub(updatedClub.getNameClub());
//        return clubRepository.save(club);
//    }

    @Override
    public Club updateClub(Long id, Club updatedClub) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Club not found"));

        if (!club.getNameClub().equals(updatedClub.getNameClub())) {
            validateUniqueName(updatedClub.getNameClub());
        }

        club.setNameClub(updatedClub.getNameClub());

        return clubRepository.save(club);
    }

    @Override
    public void deleteClub(Long id) {
        clubRepository.deleteById(id);
    }

    private void validateUniqueName(String name) {
        if (clubRepository.existsByNameClub(name)) {
            throw new DuplicateException("Club with this name already exists");
        }
    }

}
