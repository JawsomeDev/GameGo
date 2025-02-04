package com.gamego.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Time {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
