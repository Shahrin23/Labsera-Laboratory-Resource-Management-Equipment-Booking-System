package com.labresa.dao.memory;

import com.labresa.dao.ResourceDAO;
import com.labresa.model.Resource;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryResourceDAO implements ResourceDAO {

    private final Map<Integer, Resource> store = new LinkedHashMap<>();

    @Override
    public Resource findById(int id) {
        return store.get(id);
    }

    @Override
    public List<Resource> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(Resource resource) {
        store.put(resource.getId(), resource);
    }

    @Override
    public void update(Resource resource) {
        store.put(resource.getId(), resource);
    }

    @Override
    public List<Resource> findByStatus(String status) {
        return store.values().stream().filter(r -> r.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
    }
}

