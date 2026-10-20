package com.example.be.web.service.impl;

import com.example.be.web.doman.dto.response.facedata.EmbeddingResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Service
public class FaceAIClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${face.ai.base-url}")
    private String baseUrl;

//    public double[] getEmbedding(String base64) {
//
//        String url = "http://localhost:5000/embedding";
//
//        Map<String, String> body = new HashMap<>();
//        body.put("image", base64);
//
//        ResponseEntity<Map> response =
//                restTemplate.postForEntity(url, body, Map.class);
//
//        List<Double> list = (List<Double>) response.getBody().get("embedding");
//
//        return list.stream().mapToDouble(Double::doubleValue).toArray();
//    }
    public double[] getOriginalEmbedding(File imageFile) {
//        String url = "http://localhost:5000/original_embedding";
        String url = baseUrl + "/original_embedding";

        FileSystemResource resource = new FileSystemResource(imageFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, requestEntity, Map.class);

        List<Double> list = (List<Double>) response.getBody().get("embedding");
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public EmbeddingResultDto getAttendanceEmbedding(File imageFile) {
//        String url = "http://localhost:6000/attendance_embedding";
        String url = baseUrl + "/attendance_embedding";
        FileSystemResource resource = new FileSystemResource(imageFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, requestEntity, Map.class);
        Map body1 = response.getBody();

        String status = (String) body1.get("message");
        List<Double> list = (List<Double>) body1.get("embedding");

        double[] emb = null;
        if (list != null) {
            emb = list.stream().mapToDouble(Double::doubleValue).toArray();
        }

        return new EmbeddingResultDto(emb, status);

    }


}