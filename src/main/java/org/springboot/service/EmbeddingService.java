package org.springboot.service;

import org.springframework.stereotype.Service;

@Service
public interface EmbeddingService {
    float[] generateEmbedding(String content);
}
