package org.w.b01.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.w.b01.security.dto.MemberSecurityDTO;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("----------------------------");
        log.info("CustomLoginSuccessHandler onAuthenticationSuccess --------------");
        log.info(authentication.getPrincipal());
        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();
        String encodedPw = memberSecurityDTO.getMpw();
        if (memberSecurityDTO.isSocial() && memberSecurityDTO.getMpw().equals("1111")
                || passwordEncoder.matches("1111", memberSecurityDTO.getMpw())) {
            log.info("Should Change Password");
            log.info("Redirect to Member Modify");
            //response.sendRedirect("/member/modify"); // 패스워드 수정 페이지 만들면 이용
            response.sendRedirect("board/list");
            return;
        } else {
            response.sendRedirect("/board/list");
        }
    }
}
