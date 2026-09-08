package com.labresa.dao;

import java.util.List;

import com.labresa.model.Resource;


public interface ResourceDAO {
    Resource findById(int id);
    List<Resource> findAll();
    void save(Resource resource);
    void update(Resource resource);
    List<Resource> findByStatus(String status);
}
