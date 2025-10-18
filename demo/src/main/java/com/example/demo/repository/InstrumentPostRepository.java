package com.example.demo.repository;

import com.example.demo.entity.InstrumentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentPostRepository extends JpaRepository<InstrumentPost, Long> {
}
