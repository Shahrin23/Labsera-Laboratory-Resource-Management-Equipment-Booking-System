package com.labresa.service;

import com.labresa.dao.ResourceDAO;
import com.labresa.model.Resource;
import com.labresa.patterns.ResourceFactory;

import java.util.List;

public class ResourceService {

    private final ResourceDAO resourceDAO;

    public ResourceService(ResourceDAO resourceDAO) {
        this.resourceDAO = resourceDAO;
    }

    public Resource createResource(String type, String name, Resource.Category category, int totalQuantity, String detail) {
        Resource resource = ResourceFactory.create(type, 0, name, category, totalQuantity, detail);
        resourceDAO.save(resource);
        return resource;
    }

    public List<Resource> listAll() {
        return resourceDAO.findAll();
    }

    public List<Resource> listAvailable() {
        return resourceDAO.findByStatus("AVAILABLE");
    }

    public Resource findById(int id) {
        return resourceDAO.findById(id);
    }
}
