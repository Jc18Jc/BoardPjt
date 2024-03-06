package org.w.b01.service;

import org.w.b01.dto.PageRequestDTO;
import org.w.b01.dto.PageResponseDTO;
import org.w.b01.dto.ReplyDTO;

public interface ReplyService {
    Long register(ReplyDTO replyDTO);

    void modify(ReplyDTO replyDTO);

    ReplyDTO read(Long rno);

    void remove(Long rno);

    PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO);
}
