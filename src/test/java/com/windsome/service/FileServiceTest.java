package com.windsome.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks private FileService fileService;

    @Test
    @DisplayName("파일 업로드 성공")
    void testUploadFile_Success() throws Exception {
        // Given
        String uploadPath = "C:/shop/product";
        String originalFileName = "test.jpg";
        byte[] fileData = new byte[]{1, 2, 3, 4, 5};
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid + extension;
        String expectedFileUploadFullUrl = uploadPath + "/" + savedFileName;

        // When
        String result = fileService.uploadFile(uploadPath, originalFileName, fileData);

        // Then
        assertNotNull(result);
        File uploadedFile = new File(uploadPath + "/" + result);
        assertTrue(uploadedFile.exists());
        uploadedFile.delete(); // 테스트 파일 삭제
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void testDeleteFile_Success() throws Exception {
        // Given
        String uploadPath = "C:/shop/product";
        String originalFileName = "test.jpg";
        byte[] fileData = new byte[]{1, 2, 3, 4, 5};
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid + extension;

        // When
        String result = fileService.uploadFile(uploadPath, originalFileName, fileData);
        fileService.deleteFile(uploadPath + "/" + result);

        // Then
        File deletedFile = new File(uploadPath + "/" + result);
        assertFalse(deletedFile.exists());
    }

}