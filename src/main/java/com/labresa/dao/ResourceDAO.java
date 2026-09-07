package com.labresa.dao;

import com.labresa.model.Resource;

import java.util.List;
import java.util.Optional;


public interface ResourceDAO {
    Resource findById(int id);
    List<Resource> findAll();
    void save(Resource resource);
    void update(Resource resource);
    List<Resource> findByStatus(String status);
}
