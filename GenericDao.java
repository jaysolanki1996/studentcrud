package com.student.infra.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.student.model.GenericModel;

@Repository
public class GenericDao<T extends GenericModel> {

	@Autowired
	protected HibernateTemplate hibernateTemplate;

	public T save(T model) {
		try {
			hibernateTemplate.saveOrUpdate(model);
		} catch (Exception e) {
			// userOperationContextService.warn(e, e.getMessage()); -- don't need to warn,
			// we can handle it
			hibernateTemplate.clear();
			hibernateTemplate.saveOrUpdate(model);
		}
		return model;
	}

	public T retrieve(Class<T> clazz, String id) {

		DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
		criteria.add(Restrictions.eq("id", id));
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) hibernateTemplate.findByCriteria(criteria);
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	public List<T> retrieveAll(Class<T> clazz) {

		DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
		criteria.addOrder(Order.asc("id"));
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) hibernateTemplate.findByCriteria(criteria);
		return list;
	}

}
