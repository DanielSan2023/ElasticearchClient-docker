package org.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springboot.service.EmbeddingService;
import org.springboot.service.QdrantSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/qdrant")
@RequiredArgsConstructor
public class QdrantSearchController {

    private final EmbeddingService embeddingService;
    private final QdrantSearchService qdrantSearchService;

    @GetMapping("/similar")
    public Mono<Map<String, Object>> findSimilarProducts(@RequestParam String query) {
        float[] embedding = embeddingService.generateEmbedding(query);
        return qdrantSearchService.searchSimilarProducts(embedding, 5);
    }
}