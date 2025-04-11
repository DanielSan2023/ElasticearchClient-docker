package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private final ElasticsearchClient client;

    public ElasticsearchServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    public <T> T getById(String index, String id, Class<T> clazz) throws NoSuchElementException {
        GetRequest request = new GetRequest.Builder()
                .index(index)
                .id(id)
                .build();

        try {
            GetResponse<T> response = client.get(request, clazz);
            if (response.found()) {
                return response.source();
            } else {
                throw new NoSuchElementException(clazz.getSimpleName() + " with ID: " + id + " not found");
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem finding " + clazz.getSimpleName() + " with ID: " + id, e);
        }
    }

    public <T> List<T> getAll(String index, Class<T> clazz) {
        SearchRequest request = new SearchRequest.Builder()
                .index(index)
                .size(100)
                .build();
        try {
            SearchResponse<T> response = client.search(request, clazz);
            List<T> result = new ArrayList<>();
            for (Hit<T> hit : response.hits().hits()) {
                result.add(hit.source());
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the request for index " + index + ": " + e.getMessage(), e);
        }
    }

    public <T> boolean deleteById(String index, String id) throws NoSuchElementException {
        DeleteRequest request = new DeleteRequest.Builder()
                .index(index)
                .id(id)
                .build();

        DeleteResponse response = null;
        try {
            response = client.delete(request);
            if (response.result() == Result.Deleted) {
                return true;
            } else {
                throw new NoSuchElementException("Item with ID " + id + " not found in index " + index);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with deleting item with ID: " + id + " from index: " + index, e);
        }
    }
}
