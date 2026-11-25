package com.dances.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "judges")
public class Judge extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_judge")
    @Length(min = 1, max = 3, message = "*Please code have 3 symbol")
    @NotEmpty(message = "*Please provide code of judge")
    private String nameJudge;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column
    private String comment;

}
