package ru.max.springboot.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OpenGraphDataDTO {
    private String title;
    private String url;
    private String description;
    private String image;
}
