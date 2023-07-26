package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}", file);
        //get original file name
        String originalFilename = file.getOriginalFilename();

        //construct unique filename: uuid
        assert originalFilename != null;
        int idx = originalFilename.lastIndexOf(".");
        String extName = originalFilename.substring(idx);
        String newName = UUID.randomUUID() + extName;
        log.info("the new name is : {}", newName);

        //store on local disk
        String storePath = "F:\\WebDevelopment\\firmament\\img\\" + newName;
        file.transferTo(new File(storePath));
        return Result.success(storePath);
    }
}
