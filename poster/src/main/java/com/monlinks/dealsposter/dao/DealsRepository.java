package com.monlinks.dealsposter.dao;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.monlinks.dealsposter.model.Deal;

public interface DealsRepository extends MongoRepository<Deal, String> {

    //public Deal findByPostedDate(String postDate);
    public List<Deal> findByPostedDateAndCategory(String postDate,String category,Pageable pageable);
    public List<Deal> findByPostedDateAndPosterId(String postDate,String posterId,Pageable pageable);
    
    public List<Deal> deleteByPostedDate(String postDate);

	public List<Deal> deleteByAffUrl(String string);
    
}