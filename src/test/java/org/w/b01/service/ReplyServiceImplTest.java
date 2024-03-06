package org.w.b01.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w.b01.dto.ReplyDTO;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
class ReplyServiceImplTest {

    @Autowired
    private ReplyService replyService;

    @Test
    public void testReplyRegister() {
        ReplyDTO dto = ReplyDTO.builder()
                .replyText("댓글 테스트")
                .replyer("재철")
                .bno(101L)
                .build();
        Long rno = replyService.register(dto);
        log.info(dto);
        log.info(rno);
    }
}