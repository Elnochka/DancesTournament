package com.dances.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "active_dance_state")
@Getter
@Setter
@NoArgsConstructor
public class ActiveDanceState {

    @Id
    private Long id;

    @Column(name = "category_tournament_id", nullable = false)
    private Long categoryTournamentId;

    @Column(name = "round_settings_id", nullable = false)
    private Long roundSettingsId;

    @Column(name = "dance_id")
    private Long danceId;
}
