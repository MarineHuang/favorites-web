package com.sqss.voice.schedule;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sqss.voice.entity.Audio;
import com.sqss.voice.repository.AudioRepository;
import com.sqss.voice.service.NotRealTimeVoiceRecognizeService;


@Component
public class PollingTasks {
	protected Logger logger =  LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AudioRepository audioRepository;
	@Autowired
	private NotRealTimeVoiceRecognizeService nrtVoiceRecognizeService;
	
    // 一次遍历结束后10秒再进行第二次遍历
    @Scheduled(cron="0/10 * * * * ?")
    public void pollingAudioStatus() {
    	List<Audio> audiolist = audioRepository.findAll();
    	for (Audio audio : audiolist) {
    		
    		switch(audio.getStatus()) {
    		case 0:
    		case 1: // 已经完成转码
    		case 4: // 前一次转写失败了
    			logger.info("audio: " + audio.getFileFullPath() + " to upload...");
    			nrtVoiceRecognizeService.uploadAudioFile(audio);
    			break;
    		case 2:
    			logger.info("audio: " + audio.getFileFullPath() + " to getProgress...");
    			nrtVoiceRecognizeService.getProgress(audio);
    			break;
    		case 3:
    			logger.info("audio: " + audio.getFileFullPath() + " to getResult...");
    			nrtVoiceRecognizeService.getResult(audio);
    			break;
    		default:
    			
    		}
    	}

    }
}
