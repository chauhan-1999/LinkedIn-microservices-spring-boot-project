package com.chauhan.linkedInProject.post_service.client;

import com.chauhan.linkedInProject.post_service.config.FeignMultipartConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "uploader-service",
        configuration = FeignMultipartConfig.class
)
public interface UploaderServiceClient {

    @PostMapping(
            value = "/uploads/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    String uploadFile(@RequestPart("file") MultipartFile file);
}
