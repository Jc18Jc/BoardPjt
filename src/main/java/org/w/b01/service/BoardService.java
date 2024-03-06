package org.w.b01.service;


import org.w.b01.domain.Board;
import org.w.b01.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public interface BoardService {
    long register(BoardDTO dto);
    BoardDTO readOne(long bno);
    void modify(BoardDTO dto);
    void remove(long bno);
    PageResponseDTO<BoardListReplyCountDTO> listWithReplyCount(PageRequestDTO pageRequestDTO);

    PageResponseDTO<BoardListAllDTO> listWithAll(PageRequestDTO pageRequestDTO);

    default Board dtoToEntity(BoardDTO dto) {
        Board board = Board.builder()
                .bno(dto.getBno())
                .title(dto.getTitle())
                .writer(dto.getWriter())
                .content(dto.getContent())
                .build();
        if (dto.getFileNames() != null) {
            dto.getFileNames().forEach(fileName -> {
                String[] arr = fileName.split("_");
                board.addImage(arr[0], arr[1]);
            });
        }
        return board;
    }

    default BoardDTO entityToDTO(Board board) {
        BoardDTO dto = BoardDTO.builder()
                .bno(board.getBno())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .regDate(board.getRegDate())
                .modDate(board.getModDate())
                .build();
        List<String> fileNames = board.getImageSet().stream().sorted().map(boardImage -> boardImage.getUuid()+"_"+boardImage.getFileName()).collect(Collectors.toList());
        dto.setFileNames(fileNames);
        return dto;
    }
}
