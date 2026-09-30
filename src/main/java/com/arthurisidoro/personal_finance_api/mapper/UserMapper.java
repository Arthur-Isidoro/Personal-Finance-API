package com.arthurisidoro.personal_finance_api.mapper;

import com.arthurisidoro.personal_finance_api.dto.response.UserPreferencesResponse;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public UserPreferencesResponse toPreferencesResponse(User user) {
        return new UserPreferencesResponse(
                user.getCurrency().name(),
                user.getLanguage()
        );
    }
}