package org.w.b01.service;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w.b01.domain.Board;
import org.w.b01.dto.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class BoardServiceImplTest {
    @Autowired
    private BoardService boardService;

    @Test
    public void testRegister() {
        log.info(boardService.getClass().getName());
        BoardDTO dto = BoardDTO.builder()
                .title("Sample Title")
                .content("Sample Content")
                .writer("Sample Writer")
                .build();
        dto.setFileNames(
                Arrays.asList(
                        UUID.randomUUID()+"_aaa.jpg",
                        UUID.randomUUID()+"_bbb.jpg",
                        UUID.randomUUID()+"_ccc.jpg"
                ));
        long bno = boardService.register(dto);
        log.info(bno);
    }

    @Test
    public void testReadOne() {
        long bno = 101L;
        BoardDTO dto = boardService.readOne(bno);
        log.info(dto);
    }

    @Test
    public void testModify() {
        BoardDTO dto = BoardDTO.builder()
                .bno(102L)
                .title("Real Sample Title")
                .content("Real Sample Content")
                .build();
        dto.setFileNames(Arrays.asList(UUID.randomUUID()+"_zzz.jpg"));
        boardService.modify(dto);
    }

    @Test
    public void testReadAll() {
        Long bno = 102L;
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        for (String fileName: boardDTO.getFileNames()) {
            log.info(fileName);
        }
    }

    @Test
    public void testRemoveAll() {
        Long bno = 1L;
        boardService.remove(bno);
    }

    @Test
    public void testListWith() {
        PageRequestDTO requestDTO = PageRequestDTO.builder()
                .size(10)
                .page(1)
                .build();
        PageResponseDTO<BoardListAllDTO> responseDTO = boardService.listWithAll(requestDTO);
        List<BoardListAllDTO> dtoList = responseDTO.getDtoList();
        dtoList.forEach(boardListAllDTO -> {
            log.info(boardListAllDTO.getBno()+":"+boardListAllDTO.getTitle());
            if (boardListAllDTO.getBoardImages() != null) {
                for(BoardImageDTO boardImageDTO: boardListAllDTO.getBoardImages()) {
                    log.info(boardImageDTO);
                }
            }
        });
    }
}