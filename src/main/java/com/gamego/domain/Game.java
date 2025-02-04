package com.gamego.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;


@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;


}
