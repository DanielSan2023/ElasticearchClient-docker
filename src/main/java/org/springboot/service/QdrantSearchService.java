package org.springboot.service;

import org.springboot.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface QdrantSearchService {

    void saveEmbeddingToQdrant(Product product, float[] embedding);

    Mono<Map<String, Object>> searchSimilarProducts(float[] vector, int limit);

    Mono<List<String>> searchByEmbedding(float[] embedding, int topK);
}
