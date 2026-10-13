package com.example.be.web.service.impl;

import com.example.be.web.doman.dto.response.facedata.EmbeddingResultDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.doman.entity.FaceData;
import com.example.be.web.doman.entity.User;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.FaceDataRepository;
import com.example.be.web.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaceServiceTest {

    @Mock
    private FaceDataRepository faceRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private FaceAIClient faceAIClient;

    @InjectMocks
    private FaceService faceService;

    // ==================== REGISTER ====================

    @Test
    void register_shouldSaveFaceDataSuccessfully() throws IOException {
        Long userId = 1L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        User user = mock(User.class);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        when(faceAIClient.getOriginalEmbedding(any(File.class)))
                .thenReturn(new double[]{1.0, 2.0, 3.0});

        faceService.register(userId, imageFile);

        ArgumentCaptor<FaceData> captor =
                ArgumentCaptor.forClass(FaceData.class);

        verify(faceRepo).save(captor.capture());

        FaceData savedFace = captor.getValue();

        assertEquals("[1.0,2.0,3.0]", savedFace.getFaceEncoding());
        assertSame(user, savedFace.getUser());
        assertNotNull(savedFace.getCreatedAt());

        verify(userRepo).findById(userId);
        verify(faceAIClient).getOriginalEmbedding(any(File.class));
    }

    @Test
    void register_shouldThrowNotFoundWhenUserDoesNotExist()
            throws IOException {

        Long userId = 999L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> faceService.register(userId, imageFile)
        );

        verify(faceRepo, never()).save(any(FaceData.class));
        verifyNoInteractions(faceAIClient);
    }

    @Test
    void register_shouldPropagateIOExceptionWhenUploadingFileFails()
            throws IOException {

        MultipartFile imageFile = mock(MultipartFile.class);

        when(imageFile.getOriginalFilename()).thenReturn("face.jpg");

        doThrow(new IOException("Upload failed"))
                .when(imageFile).transferTo(any(File.class));

        assertThrows(
                IOException.class,
                () -> faceService.register(1L, imageFile)
        );

        verifyNoInteractions(userRepo, faceRepo, faceAIClient);
    }

    // ==================== RECOGNIZE ====================

    @Test
    void recognize_shouldReturnUnknownWhenEmbeddingIsNull()
            throws IOException {

        Long userId = 1L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        EmbeddingResultDto embeddingResult = mock(EmbeddingResultDto.class);

        when(faceAIClient.getAttendanceEmbedding(any(File.class)))
                .thenReturn(embeddingResult);

        when(embeddingResult.getEmbedding()).thenReturn(null);
        when(embeddingResult.getStatus()).thenReturn("NO_FACE_DETECTED");

        FaceResponse result = faceService.recognize(imageFile, userId);

        assertNotNull(result);
        assertEquals("Unknown", result.getName());
        assertEquals(0.0, result.getConfidence(), 0.000001);
        assertEquals("NO_FACE_DETECTED", result.getMessage());

        verifyNoInteractions(faceRepo);
    }

    @Test
    void recognize_shouldReturnRecognizedUserWhenSimilarityIsHigh()
            throws IOException {

        Long userId = 1L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        EmbeddingResultDto embeddingResult = mock(EmbeddingResultDto.class);

        when(faceAIClient.getAttendanceEmbedding(any(File.class)))
                .thenReturn(embeddingResult);

        when(embeddingResult.getEmbedding())
                .thenReturn(new double[]{1.0, 0.0});

        when(embeddingResult.getStatus()).thenReturn("SUCCESS");

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        FaceData faceData = mock(FaceData.class);
        when(faceData.getFaceEncoding()).thenReturn("[1.0,0.0]");
        when(faceData.getUser()).thenReturn(user);

        when(faceRepo.findOneByUserId(userId))
                .thenReturn(Optional.of(faceData));

        FaceResponse result = faceService.recognize(imageFile, userId);

        assertNotNull(result);
        assertEquals("testuser", result.getName());
        assertEquals(1.0, result.getConfidence(), 0.000001);
        assertEquals("SUCCESS", result.getMessage());

        verify(faceRepo).findOneByUserId(userId);
    }

    @Test
    void recognize_shouldReturnUnknownWhenSimilarityIsLow()
            throws IOException {

        Long userId = 1L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        EmbeddingResultDto embeddingResult = mock(EmbeddingResultDto.class);

        when(faceAIClient.getAttendanceEmbedding(any(File.class)))
                .thenReturn(embeddingResult);

        when(embeddingResult.getEmbedding())
                .thenReturn(new double[]{0.0, 1.0});

        when(embeddingResult.getStatus()).thenReturn("SUCCESS");

        FaceData faceData = mock(FaceData.class);
        when(faceData.getFaceEncoding()).thenReturn("[1.0,0.0]");

        when(faceRepo.findOneByUserId(userId))
                .thenReturn(Optional.of(faceData));

        FaceResponse result = faceService.recognize(imageFile, userId);

        assertNotNull(result);
        assertEquals("Unknown", result.getName());
        assertEquals(0.0, result.getConfidence(), 0.000001);

        verify(faceRepo).findOneByUserId(userId);
    }

    @Test
    void recognize_shouldThrowRuntimeExceptionWhenFaceDataNotFound()
            throws IOException {

        Long userId = 999L;

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        EmbeddingResultDto embeddingResult = mock(EmbeddingResultDto.class);

        when(faceAIClient.getAttendanceEmbedding(any(File.class)))
                .thenReturn(embeddingResult);

        when(embeddingResult.getEmbedding())
                .thenReturn(new double[]{1.0, 0.0});

        when(faceRepo.findOneByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> faceService.recognize(imageFile, userId)
        );
    }

    @Test
    void recognize_shouldPropagateIOExceptionWhenUploadingFileFails()
            throws IOException {

        MultipartFile imageFile = mock(MultipartFile.class);

        when(imageFile.getOriginalFilename()).thenReturn("face.jpg");

        doThrow(new IOException("Upload failed"))
                .when(imageFile).transferTo(any(File.class));

        assertThrows(
                IOException.class,
                () -> faceService.recognize(imageFile, 1L)
        );

        verifyNoInteractions(faceRepo, faceAIClient);
    }
}