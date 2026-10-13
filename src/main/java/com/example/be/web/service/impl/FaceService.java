package com.example.be.web.service.impl;


import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.response.facedata.EmbeddingResultDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.doman.entity.FaceData;
import com.example.be.web.doman.entity.User;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.FaceDataRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.security.UserPrincipal;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDataRepository faceRepo;
    private final UserRepository userRepo;
    private final FaceAIClient faceAIClient;

    private final Gson gson = new Gson();

    // ===============================
    // REGISTER
    // ===============================
    public void register(Long userId, MultipartFile imageFile) throws IOException {
        File tempFile = File.createTempFile("upload-", imageFile.getOriginalFilename());
        imageFile.transferTo(tempFile);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND));

        double[] embedding = faceAIClient.getOriginalEmbedding(tempFile);

        FaceData face = FaceData.builder()
                .faceEncoding(gson.toJson(embedding))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        faceRepo.save(face);
    }

    // ===============================
    // RECOGNIZE
    // ===============================
    public FaceResponse recognize(MultipartFile imageFile, Long userId) throws IOException {

        File tempFile = File.createTempFile("upload-", imageFile.getOriginalFilename());
        imageFile.transferTo(tempFile);
//        double[] input = faceAIClient.getAttendanceEmbedding(tempFile);
        EmbeddingResultDto result = faceAIClient.getAttendanceEmbedding(tempFile);

        if (result.getEmbedding() == null) {
            return FaceResponse.builder()
                    .name("Unknown")
                    .confidence(0.0)
                    .message(result.getStatus())
                    .build();
        }

        double[] input = result.getEmbedding();

//        List<FaceData> all = faceRepo.findAll();
        // Lấy dữ liệu embedding theo userId với trường hợp 1 user có nhiều embbeding
//        List<FaceData> faceDataList = faceRepo.findByUserId(userId);
        Optional<FaceData> userFace = Optional.ofNullable(faceRepo.findOneByUserId(userId)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.FaceData.ERR_NOT_FOUND_USERID)));

        double best = 0;
        User bestUser = null;

        if (userFace.isPresent()){
            FaceData f = userFace.get();
            double[] db = gson.fromJson(f.getFaceEncoding(), double[].class);
            double score = cosine(input, db);
            if (score > best) {
                best = score;
                bestUser = f.getUser();
            }
        }


//        for (FaceData f : faceDataList) {
//            //danh cho du lieu embbeding da dua ve mang 1D
//            double[] db = gson.fromJson(f.getFaceEncoding(), double[].class);
//
//            double score = cosine(input, db);
//
//            if (score > best) {
//                best = score;
//                bestUser = f.getUser();
//            }
//        }

//        String name = (best > 0.6 && bestUser != null)
        String name = (best > 0.6)
                ? bestUser.getUsername()
                : "Unknown";

        return FaceResponse.builder()
                .name(name)
                .confidence(best)
                .message(result.getStatus())
                .build();
    }

    // ===============================
    // COSINE
    // ===============================
    private double cosine(double[] a, double[] b) {

        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}