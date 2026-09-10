package com.labresa.dao;

import com.labresa.model.Resource;

import java.util.List;


public interface ResourceDAO {
    Resource findById(int id);
    List<Resource> findAll();
    void save(Resource resource);
    void update(Resource resource);
    List<Resource> findByStatus(String status);
}
