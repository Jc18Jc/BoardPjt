package org.w.b01.repository;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.w.b01.domain.Board;
import org.w.b01.domain.BoardImage;
import org.w.b01.dto.BoardListAllDTO;
import org.w.b01.dto.BoardListReplyCountDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
class BoardRepositoryTest {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @Test
    public void testInsert() {
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Board board = Board.builder()
                    .title("title..." + i)
                    .content("content..." + i)
                    .writer("writer..." + i)
                    .build();
            boardRepository.save(board);
        });
    }

    @Test
    public void testSelect() {
        Long bno = 83L;

        Optional<Board> result = boardRepository.findById(bno);
        log.info(result);
    }

    @Test
    public void testUpdate() {
        Long bno = 83L;
        Optional<Board> result = boardRepository.findById(bno);
        Board board = result.orElseThrow();
        board.change("update..title 83", "update..content 83");
        boardRepository.save(board);
    }

    @Test
    public void testDelete() {
        Long bno = 1L;
        boardRepository.deleteById(bno);
    }

    @Test
    public void testPaging() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("bno").descending());
        Page<Board> result = boardRepository.findAll(pageable);
        log.info("total count: " + result.getTotalElements());
        log.info("totalPage count: " + result.getTotalPages());
        log.info("page number: " +result.getNumber());
        log.info("page size: " + result.getSize());

        List<Board> todos = result.getContent();
        todos.forEach(board -> log.info(board));
    }

    @Test
    public void testSearch1() {
        Pageable pageable = PageRequest.of(1, 10, Sort.by("bno").descending());
        boardRepository.search1(pageable);
    }

    @Test
    public void testSearchAll() {
        Pageable pageable = PageRequest.of(1, 10, Sort.by("bno").descending());
        String[] types = {"t", "c"};
        String keyword = "1";
        boardRepository.searchAll(types, keyword, pageable);
    }

    @Test
    public void testSearchAll2() {
        Pageable pageable = PageRequest.of(1, 10, Sort.by("bno").descending());
        String[] types = {"t", "c", "w"};
        String keyword = "1";
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);
        log.info(result.getTotalPages());
        log.info(result.getSize());
        log.info(result.getNumber());
        log.info(result.hasPrevious() + ": " + result.hasNext());
        result.getContent().forEach(co -> log.info(co));
    }

    @Test
    public void testSearchReplyCount() {
        String[] types=  {"t", "c","w"};
        String keyword = "1";
        Pageable pageable = PageRequest.of(0,20,Sort.by("bno").descending());
        Page<BoardListReplyCountDTO> result =boardRepository.searchWithReplyCount(types, keyword, pageable);
        log.info(result.getTotalPages());
        log.info(result.getSize());
        log.info(result.getNumber());
        log.info(result.hasPrevious() + ": " + result.hasNext());
        result.getContent().forEach(board -> log.info(board));
    }

    @Test
    public void testInsertWithImages() {
        Board board = Board.builder()
                .title("Image Board")
                .writer("재철")
                .content("첨부파일 테스트")
                .build();
        for (int i = 0; i<3; i++) {
            board.addImage(UUID.randomUUID().toString(),"file"+i+".jpg");
        }
        boardRepository.save(board);
    }

    @Test
    public void testReadWithImage() {
        Optional<Board> result = boardRepository.findByIdWithImages(1L);
        Board board = result.orElseThrow();

        log.info(board);
        for (BoardImage boardImage : board.getImageSet()) {
            log.info(boardImage);
        }
    }

    @Transactional
    @Commit
    @Test
    public void testModifyImages() {
        Optional<Board> result = boardRepository.findByIdWithImages(1L);
        Board board = result.orElseThrow();

        board.clearImages();
        for (int i = 3; i < 6; i++) {
            board.addImage(UUID.randomUUID().toString(), "new image"+i+".jpg");
        }
        boardRepository.save(board);
    }

    @Test
    @Transactional
    @Commit
    public void testRemoveAll() {
        Long bno= 1L;
        replyRepository.deleteByBoard_Bno(bno);
        boardRepository.deleteById(bno);
    }

    @Test
    public void testInsertAll() {
        for (int i = 1; i <= 100; i++) {
            Board board = Board.builder()
                    .title("Title.."+i)
                    .content("Content.."+i)
                    .writer("Writer.."+i)
                    .build();

            for (int j = 0; j < 3; j++) {
                if (i%5==0) continue;
                board.addImage(UUID.randomUUID().toString(), "image"+i+"_"+j);
            }
            boardRepository.save(board);
        }
    }

    @Test
    @Transactional
    public void testSearchImageReplyCount() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("bno").descending());
        Page<BoardListAllDTO> result = boardRepository.searchWithAll(null, null, pageable);
        log.info("--------------------------");
        log.info(result.getTotalElements());
        result.getContent().forEach(dto -> log.info(dto));
    }
}