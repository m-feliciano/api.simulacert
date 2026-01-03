package com.simulacert.auth.application.mapper;

import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}

