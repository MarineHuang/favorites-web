package com.sqss.voice.entity;

import javax.persistence.*;
import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Audio implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true)
	private String fileFullPath;
	@Column(nullable = false)
	private Long uploadTime;
	/*
	 * 0 初始值
	 * 1 转码成功后，set 1
	 * 2 上传音频到转写服务器成功后, set status=2, && set taskID
	 * 3 根据taskID查询，得知转写完成，获得转写结果，set status=3
	 * 4 根据taskID查询，得知转写失败后，set status=4
	 * 5 根据taskID得到转写结果后，set status=5
	 */
	@Column(nullable = false)
	private int status;
	@Column(nullable = true)
	private String taskId;
	@Column(nullable = true)
	private String resultFullPath;
	
	public Audio() {
		
	}
	
	public Audio(String fileFullPath,Long uploadTime) {
		this.fileFullPath = fileFullPath;
		this.uploadTime = uploadTime;
		this.status = 0;
	}
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getFileFullPath() {
		return fileFullPath;
	}


	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}


	public Long getUploadTime() {
		return uploadTime;
	}


	public void setUploadTime(Long uploadTime) {
		this.uploadTime = uploadTime;
	}


	public String getTaskId() {
		return taskId;
	}


	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public String getResultFullPath() {
		return resultFullPath;
	}


	public void setResultFullPath(String resultFullPath) {
		this.resultFullPath = resultFullPath;
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
