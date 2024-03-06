package org.w.b01.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.w.b01.dto.PageRequestDTO;
import org.w.b01.dto.PageResponseDTO;
import org.w.b01.dto.ReplyDTO;
import org.w.b01.service.ReplyService;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
@Log4j2
public class ReplyController {

    private final ReplyService replyService;

    @Operation(summary = "Replies POST")
    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> register(@Valid @RequestBody ReplyDTO replyDTO, BindingResult bindingResult) throws BindException {
        log.info(replyDTO);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        Map<String, Long> resultMap = new HashMap<>();
        Long rno = replyService.register(replyDTO);
        resultMap.put("rno",rno);
        return resultMap;
    }

    @Operation(summary = "Replies of Board")
    @GetMapping(value = "/list/{bno}")
    public PageResponseDTO<ReplyDTO> getList(@PathVariable("bno") Long bno, PageRequestDTO pageRequestDTO) {
        PageResponseDTO<ReplyDTO> pageResponseDTO = replyService.getListOfBoard(bno, pageRequestDTO);
        return pageResponseDTO;
    }

    @Operation(summary = "Read Reply")
    @GetMapping("/{rno}")
    public ReplyDTO getReply(@PathVariable("rno") Long rno) {
        ReplyDTO dto = replyService.read(rno);
        return dto;
    }

    @Operation(summary = "Remove Reply")
    @DeleteMapping("/{rno}")
    public Map<String, Long> removeReply(@PathVariable("rno") Long rno) {
        replyService.remove(rno);
        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("rno", rno);
        return resultMap;
    }

    @Operation(summary = "Modify Reply")
    @PutMapping(value = "/{rno}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> modifyReply(@PathVariable("rno") Long rno, @RequestBody ReplyDTO dto) {
        dto.setRno(rno);    // 번호를 일치시킴
        replyService.modify(dto);
        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("rno", rno);
        return resultMap;
    }
}
