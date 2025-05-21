package org.springboot.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingServiceImpl(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        try {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Text for embedding must not be null or empty.");
            }
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            return response.getResults().get(0).getOutput();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding for text: " + text, e);
        }
    }
}

