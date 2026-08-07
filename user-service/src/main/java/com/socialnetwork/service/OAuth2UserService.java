package com.socialnetwork.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.socialnetwork.dto.Oauth2UserInfoDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.model.User;
import com.socialnetwork.repository.UserRepository;
import com.socialnetwork.security.UserPrincipalConverter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @SneakyThrows
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        log.trace("Load user {}", oAuth2UserRequest);
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        return processOAuth2User(oAuth2UserRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        Oauth2UserInfoDto userInfoDto = Oauth2UserInfoDto
                .builder()
                .name(attrs.get("name") != null ? attrs.get("name").toString() : "")
                .id(attrs.get("sub") != null ? attrs.get("sub").toString() : "")
                .email(attrs.get("email") != null ? attrs.get("email").toString() : "")
                .picture(attrs.get("picture") != null ? attrs.get("picture").toString() : "")
                .build();

        Optional<User> userOptional = userRepository.findByUsername(userInfoDto.getEmail());
        User user = userOptional
                .map(existingUser -> updateExistingUser(existingUser, userInfoDto))
                .orElseGet(() -> registerNewUser(oAuth2UserRequest, userInfoDto));
        return UserPrincipalConverter.fromUser(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, Oauth2UserInfoDto userInfoDto) {
        User user = new User();
        user.setProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        user.setProviderId(userInfoDto.getId());
        user.setName(userInfoDto.getName());
        user.setUsername(userInfoDto.getEmail());
        user.setPicture(userInfoDto.getPicture());
        user.setId(UUID.randomUUID());
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, Oauth2UserInfoDto userInfoDto) {
        existingUser.setName(userInfoDto.getName());
        existingUser.setPicture(userInfoDto.getPicture());
        return userRepository.save(existingUser);
    }
}
