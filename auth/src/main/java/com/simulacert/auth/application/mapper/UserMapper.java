package com.simulacert.auth.application.mapper;

import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}

