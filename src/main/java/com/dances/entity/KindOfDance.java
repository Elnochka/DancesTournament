package com.dances.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kind_of_dances")
public class KindOfDance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_kind_of_dance")
    private String nameKindOfDance;

    @Column(name = "full_name_kind_of_dance")
    private String fullNameKindOfDance;
}
