package org.w.b01.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w.b01.dto.upload.UploadFileDTO;
import org.w.b01.dto.upload.UploadResultDTO;
import org.w.b01.util.S3Uploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class UpDownController {
    @Value("${org.w.upload.path}")
    private String uploadPath;

    private final S3Uploader s3Uploader;

    @Operation(summary = "Upload POST")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> upload(@Parameter(description = "File to be uploaded", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) UploadFileDTO uploadFileDTO) {
        log.info(uploadFileDTO);

        if (uploadFileDTO.getFiles() != null) {
            final List<UploadResultDTO> list = new ArrayList<>();
            uploadFileDTO.getFiles().forEach(multipartFile -> {
                String originalName = multipartFile.getOriginalFilename();
                log.info(originalName);
                String uuid = UUID.randomUUID().toString();
                boolean img = false;
                Path savePath = Paths.get(uploadPath, uuid+"_"+originalName);
                try {
                    multipartFile.transferTo(savePath); // 실제 저장
                    File saveFile = savePath.toFile();
                    s3Uploader.upload(saveFile.getAbsolutePath());
                    if (Files.probeContentType(savePath).startsWith("image")) {
                        img=true;
                        File thumbFile = new File(uploadPath, "s_" + uuid + "_" + originalName);
                        Path thumbPath = Paths.get(uploadPath, "s_" + uuid + "_" + originalName);
                        Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
                        s3Uploader.upload(thumbPath.toFile().getAbsolutePath());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                UploadResultDTO resultDTO = UploadResultDTO.builder()
                        .img(img)
                        .uuid(uuid)
                        .fileName(originalName)
                        .build();
                list.add(resultDTO);

            });

            return list;
        }
        return null;
    }

    @Operation(summary = "view 파일")
    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable(name = "fileName") String fileName) {
        Resource resource = new FileSystemResource(uploadPath+File.separator+fileName);
        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @Operation(summary = "remove 파일")
    @DeleteMapping("/remove/{fileName}")
    public Map<String, Boolean> removeFile(@PathVariable(name="fileName") String fileName) {
        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
        String resourceName = resource.getFilename();
        Map<String , Boolean> resultMap = new HashMap<>();
        boolean removed = false;

        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            removed = resource.getFile().delete();
            s3Uploader.removeS3File(resourceName);
            if (contentType.startsWith("image")) {
                File thumbnailFile = new File(uploadPath+File.separator+"s_"+fileName);
                thumbnailFile.delete();
                s3Uploader.removeS3File(thumbnailFile.getName());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        resultMap.put("result", removed);
        return resultMap;
    }
}
