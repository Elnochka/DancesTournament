package com.dances.entity;

import com.dances.model.GenderEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstname_ukr", length = 100)
    private String firstnameUkr;

    @Column(name = "lastname_ukr", length = 100)
    private String lastnameUkr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "participant_kind_of_dance",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "kind_of_dance_id")
    )
    private Set<KindOfDance> kindOfDances = new HashSet<>();

    @Column(name = "date_birth")
    private LocalDate dateBirth;

    @Transient
    public Integer getAge() {
        if (dateBirth == null) return null;
        return Period.between(dateBirth, LocalDate.now()).getYears();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private GenderEnum gender;

    @Column(name = "firstname_en", length = 100)
    private String firstnameEn;

    @Column(name = "lastname_en", length = 100)
    private String lastnameEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klass_id", nullable = false)
    private Klass klass;

    public String getDisplayNameUkr() {

            return firstnameUkr + " " + lastnameUkr;

    }

    public String getDisplayNameEn() {

        return firstnameEn + " " + lastnameEn;

    }
}
