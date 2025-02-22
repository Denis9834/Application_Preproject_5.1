package ru.max.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotEmpty(message = "Поле не должно быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть в диапазоне от 2 до 50 символов")
    private String name;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Email(message = "Email должен быть валидным")
    private String email;

    @NotNull(message = "Поле не должно быть пустым")
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    private Integer age;

    private String password;

    private List<Long> roleId;

}
