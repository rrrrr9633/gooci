package com.gooci.console.support;

import com.gooci.console.common.BusinessException;
import com.gooci.console.web.dto.FileSavedView;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName CertFileStore
 * @Description 认证附件落在企业目录，不进数据库大字段
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class CertFileStore {

    private static final long MAX_BYTES = 4L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "gif", "png", "pdf");

    private final Path root = Path.of("data", "uploads");

    public FileSavedView save(String enterId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择附件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("附件大小不超过4M");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED.contains(ext)) {
            throw new BusinessException("请上传jpg、jpeg、gif、png、pdf");
        }
        String fileId = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = root.resolve(enterId);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileId).toAbsolutePath());
        } catch (IOException ex) {
            throw new BusinessException("附件保存失败");
        }
        FileSavedView view = new FileSavedView();
        view.setFileId(fileId);
        view.setFileName(file.getOriginalFilename());
        return view;
    }

    public Resource load(String enterId, String fileId) {
        Path path = resolve(enterId, fileId);
        if (!Files.isRegularFile(path)) {
            throw new BusinessException("附件不存在");
        }
        return new FileSystemResource(path);
    }

    public MediaType mediaTypeOf(String fileId) {
        String ext = extensionOf(fileId);
        if ("pdf".equals(ext)) {
            return MediaType.APPLICATION_PDF;
        }
        if ("png".equals(ext)) {
            return MediaType.IMAGE_PNG;
        }
        if ("gif".equals(ext)) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }

    private Path resolve(String enterId, String fileId) {
        if (fileId == null || fileId.contains("/") || fileId.contains("\\") || fileId.contains("..")) {
            throw new BusinessException("附件不存在");
        }
        return root.resolve(enterId).resolve(fileId).normalize();
    }

    private String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
