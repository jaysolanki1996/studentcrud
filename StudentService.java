package com.student.infra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.infra.dao.StudentDao;
import com.student.model.Student;

@Service
public class StudentService extends GenericService<Student>{

	public StudentService() {
        super(Student.class);
    }

    @Autowired
    public void setDao(StudentDao dao) {
        super.dao = dao;
    }

}
