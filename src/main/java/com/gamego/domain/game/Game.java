package com.gamego.domain.game;


import jakarta.persistence.*;
import lombok.*;


@Entity @Builder
@Setter
@Getter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
