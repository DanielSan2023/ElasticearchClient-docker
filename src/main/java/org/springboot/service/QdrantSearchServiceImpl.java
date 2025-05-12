package org.springboot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springboot.model.Product;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QdrantSearchServiceImpl implements QdrantSearchService {

    private final WebClient qdrantWebClient;

    public void saveEmbeddingToQdrant(Product product, float[] embedding) {
        if (embedding.length != 1536) {
            throw new IllegalArgumentException("Embedding must be 1536 dimensions.");
        }

        Map<String, Object> point = Map.of(
                "id", Long.parseLong(product.getEan()),
                "vector", embedding,
                "payload", Map.of(
                        "text", product.getDescription(),
                        "metadata", Map.of(
                                "source", "user"
                        )
                )
        );
        Map<String, Object> requestBody = Map.of(
                "points", List.of(point)
        );

        try {
            System.out.println(new ObjectMapper().writeValueAsString(requestBody));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        qdrantWebClient.put()
                .uri("/collections/products/points?wait=true")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> System.err.println("Error during Qdrant request: " + e.getMessage()))
                .block();
    }

    public Mono<Map<String, Object>> searchSimilarProducts(float[] vector, int limit) {
        Map<String, Object> requestBody = Map.of(
                "vector", vector,
                "top", limit
        );

        return qdrantWebClient.post()
                .uri("/collections/products/points/search")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    @Override
    public Mono<List<String>> searchByEmbedding(float[] embedding, int topK) {
        Logger logger = LoggerFactory.getLogger(getClass());

        Map<String, Object> requestBody = Map.of(
                "vector", embedding,
                "top", topK
        );

        return qdrantWebClient.post()
                .uri("/collections/products/points/search")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(responseMap -> {
                    Object rawResult = responseMap.get("result");

                    if (!(rawResult instanceof List<?> rawList)) {
                        return List.<String>of();
                    }

                    return rawList.stream()
                            .filter(Objects::nonNull)
                            .map(item -> {
                                if (item instanceof Map<?, ?>) {
                                    Object id = ((Map<?, ?>) item).get("id");
                                    if (id != null) {
                                        return id.toString();
                                    }
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .onErrorResume(e -> {
                    logger.error("Qdrant search failed", e);
                    System.err.println("Qdrant search failed: " + e.getMessage());
                    return Mono.just(List.of());
                });
    }

    public void createCollectionIfNotExists() {
        Map<String, Object> vectorsConfig = Map.of(
                "size", 1536,
                "distance", "Cosine"
        );

        Map<String, Object> body = Map.of(
                "vectors", vectorsConfig
        );

        qdrantWebClient.put()
                .uri("/collections/products")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
