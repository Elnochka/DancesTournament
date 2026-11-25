package com.dances.service;

import com.dances.entity.Club;

import java.util.List;
import java.util.Optional;

public interface ClubService {

    Club createClub(Club club);
    List<Club> getAllClubs();
    Optional<Club> getClubById(Long id);
    Club updateClub(Long id, Club updatedClub);
    void deleteClub(Long id);
}
