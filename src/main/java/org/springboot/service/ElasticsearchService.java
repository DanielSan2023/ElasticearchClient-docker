package org.springboot.service;

import java.util.List;
import java.util.NoSuchElementException;

public interface ElasticsearchService {

    public <T> T getById(String index, String id, Class<T> clazz) throws NoSuchElementException;

    public <T> List<T> getAll(String index, Class<T> clazz);

    public <T> boolean deleteById(String index, String id) throws NoSuchElementException;
}
