package com.sqss.voice.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sqss.voice.entity.Audio;
import antlr.collections.List;

public interface AudioRepository extends JpaRepository<Audio, Long> {

	 Audio findByFileFullPath(String fileFullPath);

	 Audio findById(long id);
	 
	@Modifying(clearAutomatically=true)
    @Transactional
    @Query("update Audio set status=:status where id=:id") 
    int setStatus(@Param("status") int status, @Param("id") Long id);
 
	
	@Modifying(clearAutomatically=true)
    @Transactional
    @Query("update Audio set status=:status, taskId=:taskId where id=:id") 
    int setStatusAndTaskId(@Param("status") int status, @Param("taskId") String taskId, @Param("id") Long id);
	
	@Modifying(clearAutomatically=true)
    @Transactional
    @Query("update Audio set status=:status, resultFullPath=:resultFullPath where id=:id") 
    int setStatusAndResultFullPath(@Param("status") int status, @Param("resultFullPath") String resultFullPath, @Param("id") Long id);
}
