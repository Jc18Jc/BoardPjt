package org.w.b01.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.w.b01.dto.*;
import org.w.b01.service.BoardService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/board")
@Log4j2
@RequiredArgsConstructor
public class BoardController {
    @Value("${org.w.upload.path}")
    private String uploadPath;
    private final BoardService boardService;

    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<BoardListAllDTO> responseDTO = boardService.listWithAll(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/register")
    public void registerGet() {

    }

    @PostMapping("/register")
    public String  registerPost(@Valid BoardDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.info("register has error  ................");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/board/register";
        }
        long bno = boardService.register(dto);
        redirectAttributes.addFlashAttribute("result", bno);
        return "redirect:/board/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping({"/read", "/modify"})
    // 변수명과 파라미터명이 동일한 경우 RequestParam이 필요 없지만 뭐 디버그 모드에서나 그렇고 지금 서버에 deploy한다고 에러 뜨네 ??..
    public void readGet(@RequestParam(name="bno") Long bno, PageRequestDTO pageRequestDTO, Model model) {
        log.info("read Get .......................");
        BoardDTO dto = boardService.readOne(bno);
        model.addAttribute("dto", dto);
    }

    @PreAuthorize("principal.username == #boardDTO.writer")
    @PostMapping("/modify")
    public String modifyPost(PageRequestDTO pageRequestDTO, @Valid BoardDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info(dto);
        if (bindingResult.hasErrors()) {
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("bno", dto.getBno());
            return "redirect:/board/modify?"+link;
        }
        boardService.modify(dto);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("bno", dto.getBno());
        return "redirect:/board/read";
    }

    @PreAuthorize("principal.username == #dto.writer")
    @PostMapping("/remove")
    public String  removePost(BoardDTO dto, RedirectAttributes redirectAttributes) {
        Long bno = dto.getBno();
        log.info("remove post..." + bno);
        boardService.remove(bno);
        log.info(dto.getFileNames());
        List<String> fileNames = dto.getFileNames();
        if (fileNames != null && fileNames.size() > 0) {
            removeFiles(fileNames);
        }
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/board/list";
    }

    private void removeFiles(List<String> fileNames) {
        for (String fileName: fileNames) {
            Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
            String resourceName = resource.getFilename();
            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                resource.getFile().delete();
                if (contentType.startsWith("img")) {
                    File thumbnailFile = new File(uploadPath + File.separator + "s_" + fileName);
                    thumbnailFile.delete();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
