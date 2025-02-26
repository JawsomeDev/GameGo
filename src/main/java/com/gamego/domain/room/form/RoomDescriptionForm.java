package com.gamego.domain.room.form;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RoomDescriptionForm {

    @NotBlank
    @Length(max=100)
    private String shortDescription;

    @NotBlank
    private String longDescription;
}
