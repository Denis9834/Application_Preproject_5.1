package ru.max.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;
import ru.max.springboot.util.Transliteration;


@Mapper(componentModel = "spring", uses = {Transliteration.class})
public interface InterviewMapper {

    @Mapping(target = "interviewId", ignore = true)
    @Mapping(target = "status", source = "dto.status", defaultValue = "SCHEDULED")
    @Mapping(target = "organizationLatin", source = "dto.organization", qualifiedByName = "toLatin")
    @Mapping(source = "user", target = "user")
    Interview toEntity(InterviewDTO dto, User user);

    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "telegramUsername", source = "user.telegramUsername")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "id", source = "interviewId")
    InterviewResponseDTO toDto(Interview interview);

    @Mapping(target = "interviewId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "organizationLatin", source = "dto.organization", qualifiedByName = "toLatin")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "finalOffer", source = "dto.finalOffer")
    @Mapping(target = "interviewNotes", source = "dto.interviewNotes")
    void updateInterviewFromDto(InterviewDTO dto, @MappingTarget Interview interview);
}
