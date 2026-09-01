package com.gamebox.user.controller;

import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.R;
import com.gamebox.user.config.FileProperties;
import com.gamebox.user.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private static final Set<String> ALLOWED_TYPES = Set.of("avatar", "cover", "record");
    private static final Set<String> ALLOWED_EXTS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final FileProperties fileProperties;

    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("type") String type) throws IOException {
        if (!ALLOWED_TYPES.contains(type)) {
            throw BizException.of("不支持的上传类型");
        }
        if (file.isEmpty()) {
            throw BizException.of("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = (original == null || !original.contains("."))
                ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTS.contains(ext)) {
            throw BizException.of("仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path dir = Paths.get(fileProperties.getDir(), type, dateDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));

        String url = fileProperties.getUrlPrefix() + type + "/" + dateDir + "/" + filename;
        return R.ok(FileUploadVO.builder().url(url).build());
    }
}
