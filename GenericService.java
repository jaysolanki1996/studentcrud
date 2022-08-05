package com.student.infra.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.student.infra.dao.GenericDao;
import com.student.model.GenericModel;

@Service
@Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
public abstract class GenericService<T extends GenericModel> {

	@Autowired
    @Qualifier("genericDao")
    protected GenericDao<T> dao;

    private Class<T> modelClass;
    
    public GenericService(Class<T> modelClass) {
        this.modelClass = modelClass;
    }
    
    
    private void setCreatingNewObject(T model) {
        if (model instanceof GenericModel) {
            ((GenericModel) model).setCreatingNewObject(((GenericModel) model).getId() == null);
        }
    }
    
    public T retrieve(String id) {
    	return dao.retrieve(modelClass, id);
    }
    
    // If we want any changes before save.
    public T preSave(T model) {
        setCreatingNewObject(model);
        return model;
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public T saveWithEvict(T model){
        // TO Identify Create Or Update Operation.
    	setCreatingNewObject(model);
    	
        model = preSave(model);
        model = dao.save(model);
        return model;
    }
    
    public List<T> retrieveAll() {
    	return dao.retrieveAll(modelClass);
    }
}
