package com.sqss.voice.service;

import com.sqss.voice.entity.Audio;
import com.sqss.voice.util.ApiResultDto;

public interface NotRealTimeVoiceRecognizeService {
	public String uploadAudioFile(Audio audio);
	public ApiResultDto getProgress(Audio audio);
	public String getResult(Audio audio);
	public String formatResult(Audio audio);
}
