package br.com.simulaaws.auth.application.mapper;

import br.com.simulaaws.auth.application.dto.UserResponse;
import br.com.simulaaws.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}

