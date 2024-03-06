package org.w.b01.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.w.b01.domain.Member;
import org.w.b01.domain.MemberRole;
import org.w.b01.repository.MemberRepository;
import org.w.b01.security.dto.MemberSecurityDTO;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("userRequest -----------");
        log.info(userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();
        log.info("NAME: " + clientName);
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String , Object> paramMap = oAuth2User.getAttributes();
        paramMap.forEach((k, v) -> {
            log.info("-----------------------");
            log.info(k+":"+v);
        });

        String nickName = null;
        switch (clientName) {
            case "kakao":
                nickName = getKakaoNickName(paramMap);
                break;
        }

        return generateDTO(nickName, paramMap);
    }

    private MemberSecurityDTO generateDTO(String nickName, Map<String, Object> paramMap) {
        Optional<Member> result = memberRepository.findByNickName(nickName);
        if (result.isEmpty()) {
            Member member = Member.builder()
                    .mid(nickName)
                    .mpw(passwordEncoder.encode("1111"))
                    .nickName(nickName)
                    .social(true)
                    .build();
            member.addRole(MemberRole.USER);
            memberRepository.save(member);
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(nickName, "1111", nickName, false,
                    true, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
            memberSecurityDTO.setProps(paramMap);
            return memberSecurityDTO;
        } else {
            Member member = result.get();
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(member.getMid(), member.getMpw(),
                    member.getNickName(), member.isDel(), member.isSocial(),
                    member.getRoleSet().stream().map(memberRole ->
                            new SimpleGrantedAuthority("ROLE_"+memberRole.name())).collect(Collectors.toList()));
            memberSecurityDTO.setProps(paramMap);
            return memberSecurityDTO;
        }
    }

    private String getKakaoNickName(Map<String, Object> paramMap) {
        log.info("KAKAO ---------------");
        Object value = paramMap.get("kakao_account");
        LinkedHashMap accountMap = (LinkedHashMap) value;
        log.info(accountMap);
        String nickName = (String) ((LinkedHashMap)accountMap.get("profile")).get("nickname");
        log.info("nickname...." + nickName);
        return nickName;
    }

}
