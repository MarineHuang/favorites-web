package com.sqss.voice.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sqss.voice.entity.Audio;
import com.sqss.voice.repository.AudioRepository;
import com.sqss.voice.service.NotRealTimeVoiceRecognizeService;
import com.sqss.voice.util.ApiResultDto;
import com.sqss.voice.util.EncryptUtil;
import com.sqss.voice.util.HttpUtil;
import com.sqss.voice.util.IflytekVoiceResult;
import com.sqss.voice.util.SliceIdGenerator;


@Service
@EnableAsync  //开启基于注解的异步调用功能
public class NotRealTimeVoiceRecognizeServiceImpl implements NotRealTimeVoiceRecognizeService, ApplicationRunner  {
	protected Logger logger =  LoggerFactory.getLogger(this.getClass());
	
	//Save the ASR result
	@Value("${asr.result.folder}")
    private String ASR_RESULT_FOLDER;
	
	@Autowired
	private AudioRepository audioRepository;
	
	public static final String LFASR_HOST = "https://raasr.xfyun.cn/api";

    /**
     * TODO 设置appid和secret_key
     */
    public static final String APPID = "54c25757";
    public static final String SECRET_KEY = "e800592913857124197525b1b2b7d36e";

    public static final String PREPARE = "/prepare";
    public static final String UPLOAD = "/upload";
    public static final String MERGE = "/merge";
    public static final String GET_RESULT = "/getResult";
    public static final String GET_PROGRESS = "/getProgress";

    
    /**
     * 文件分片大小,可根据实际情况调整
     */
    public static final int SLICE_SICE = 10485760;// 10M
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	 File file = new File(ASR_RESULT_FOLDER);
         if (!file.exists()) {
             file.mkdirs();
             logger.info("making directory: " + ASR_RESULT_FOLDER);
         }
    }

    
    /**
          * 获取每个接口都必须的鉴权参数
     * 
     * @return
     * @throws SignatureException 
     */
    public Map<String, String> getBaseAuthParam(String taskId) throws SignatureException {
        Map<String, String> baseParam = new HashMap<String, String>();
        baseParam.put("app_id", APPID);
        
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        baseParam.put("ts", ts);
        
        baseParam.put("signa", EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(APPID + ts), SECRET_KEY));
        
        if (taskId != null) {
            baseParam.put("task_id", taskId);
        }

        return baseParam;
    }
    

    /**
     * 预处理
     * 
     * @param audio     需要转写的音频
     * @return
     * @throws SignatureException 
     */
    public String prepare(File audio) throws SignatureException {
        Map<String, String> prepareParam = getBaseAuthParam(null);
        long fileLenth = audio.length();

        prepareParam.put("file_len", fileLenth + "");
        prepareParam.put("file_name", audio.getName());
        prepareParam.put("slice_num", (fileLenth/SLICE_SICE) + (fileLenth % SLICE_SICE == 0 ? 0 : 1) + "");

        /********************TODO 可配置参数********************/
        //语种 cn:中文（默认） en:英文（英文不支持热词
        prepareParam.put("language", "cn");
        // 转写类型，可选值： 0（标准版-已录制音频，格式包括 wav,flac,opus,mp3,m4a）， 2（电话专用版，已取消电话专用版套餐）,默认0
        prepareParam.put("lfasr_type", "0");
        // 转写结果是否包含分词信息
        prepareParam.put("has_participle", "true");
        // 转写结果中是否包含发音人分离信息
        prepareParam.put("has_seperate", "false");
        // 转写结果中最大的候选词个数
//        prepareParam.put("max_alternatives", "2");
        //发音人个数，可选值：0-10，0表示盲分
        prepareParam.put("speaker_number", "0");
        // 是否进行敏感词检出, false或true， 默认：false
//        prepareParam.put("has_sensitive", "true");
        // 敏感词类型 需要进行敏感词检测(has_sensitive为true)时必传，0(默认词库)或1(自定义敏感词)
//        prepareParam.put("sensitive_type", "1");
        // 自定义关键词，敏感词检测类型为1时必传，格式：科大讯飞，语音转写(每个词用英文逗号分割，整个字符串长度不超过256)
//        prepareParam.put("keywords", "科大讯飞,中国");
        /****************************************************/

        String response = HttpUtil.post(LFASR_HOST + PREPARE, prepareParam);
        if (response == null) {
            throw new RuntimeException("预处理接口请求失败！");
        }
        ApiResultDto resultDto = JSON.parseObject(response, ApiResultDto.class);
        String taskId = resultDto.getData();
        if (resultDto.getOk() != 0 || taskId == null) {
            throw new RuntimeException("预处理失败！" + response);
        }

        logger.info("预处理成功, taskid：" + taskId);
        return taskId;
    }

    /**
     * 分片上传
     * 
     * @param taskId        任务id
     * @param slice         分片的byte数组
     * @throws SignatureException 
     */
    public void uploadSlice(String taskId, String sliceId, byte[] slice) throws SignatureException {
        Map<String, String> uploadParam = getBaseAuthParam(taskId);
        uploadParam.put("slice_id", sliceId);

        String response = HttpUtil.postMulti(LFASR_HOST + UPLOAD, uploadParam, slice);
        if (response == null) {
            throw new RuntimeException("分片上传接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            logger.info("分片上传成功, sliceId: " + sliceId);
            return;
        }

        logger.info("params: " + JSON.toJSONString(uploadParam));
        throw new RuntimeException("分片上传失败！" + response + "|" + taskId);
    }

    /**
     * 文件合并
     * 
     * @param taskId        任务id
     * @throws SignatureException 
     */
    public void merge(String taskId) throws SignatureException {
        String response = HttpUtil.post(LFASR_HOST + MERGE, getBaseAuthParam(taskId));
        if (response == null) {
            throw new RuntimeException("文件合并接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
        	logger.info("文件合并成功, taskId: " + taskId);
            return;
        }

        throw new RuntimeException("文件合并失败！" + response);
    }
    

    
    @Async
    @Override
	public String uploadAudioFile(Audio audio) {
		File audiofile = new File(audio.getFileFullPath());
		String taskId = null;
	    try (FileInputStream fis = new FileInputStream(audiofile)) {
	        // 预处理
	        taskId = prepare(audiofile);

	        // 分片上传文件
	        int len = 0;
	        byte[] slice = new byte[SLICE_SICE];
	        SliceIdGenerator generator = new SliceIdGenerator();
	        while ((len =fis.read(slice)) > 0) {
	            // 上传分片
	            if (fis.available() == 0) {
	                slice = Arrays.copyOfRange(slice, 0, len);
	            }
	            uploadSlice(taskId, generator.getNextSliceId(), slice);
	        }
	        // 合并文件
	        merge(taskId);
	    } catch (Exception e) {
	        taskId = null;
	    	e.printStackTrace();
	    }
	   if(taskId != null) {
		   audioRepository.setStatusAndTaskId(2, taskId, audio.getId());
		   //logger.info("转写上传音频成功: " + audio.getId());
	   }
	   return taskId;
	}
	
    @Async
	@Override
	public ApiResultDto getProgress(Audio audio) {
		String response = null;
		try{
			response = HttpUtil.post(LFASR_HOST + GET_PROGRESS, getBaseAuthParam(audio.getTaskId()));
		}catch (Exception e) {
			response = null;
	        e.printStackTrace();
	    }
		
		if (response == null) {
			return null;
		}
		ApiResultDto apiResult =  JSON.parseObject(response, ApiResultDto.class);
		if (apiResult.getOk() == 0) {
            if (apiResult.getErr_no() != 0) {
                logger.warn("任务失败：" + JSON.toJSONString(apiResult));
                audioRepository.setStatus(4, audio.getId());
            }

            String taskStatus = apiResult.getData();
            if (JSON.parseObject(taskStatus).getInteger("status") == 9) {                    
                logger.info("转写任务完成！" + audio.getId());
                audioRepository.setStatus(3, audio.getId());
            }
		}
		return apiResult;
	}
	
	@Override
	public String getResult(Audio audio) {
		try {
			String responseStr = HttpUtil.post(LFASR_HOST + GET_RESULT, getBaseAuthParam(audio.getTaskId()));
	        if (responseStr == null) {
	            throw new RuntimeException("获取结果接口请求失败！");
	        }
	        ApiResultDto response = JSON.parseObject(responseStr, ApiResultDto.class);
	        if (response.getOk() != 0) {
	            throw new RuntimeException("获取结果失败！" + responseStr);
	        }
	        
	        String rst = response.getData();
	        Path path = Paths.get(ASR_RESULT_FOLDER + audio.getId() + ".txt");
            Files.write(path, rst.getBytes());
            audioRepository.setStatusAndResultFullPath(5, path.toString(), audio.getId());
            logger.info("获得转写结果: " + audio.getId());
	        
	        return rst;
		}catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
	private String readString(String filePath) {
		String str="";
		File file=new File(filePath);
		try {
		 FileInputStream in=new FileInputStream(file);
		 // size 为字串的长度 ，这里一次性读完
		 int size=in.available();
		 byte[] buffer=new byte[size];
		 in.read(buffer);
		 in.close();
		 str=new String(buffer,"UTF-8");
		} catch (IOException e) {
			 // TODO Auto-generated catch block
			e.printStackTrace(); 
			return null;
		}
		return str;
	}
	
	@Override
	public String formatResult(Audio audio) {
		try {
			String resultPath = audio.getResultFullPath();
			String jsonResult = readString(resultPath);
			logger.info("JSON格式转写结果: " + jsonResult);
			IflytekVoiceResult voiceResult = new IflytekVoiceResult(jsonResult);
			String onebestResult = voiceResult.getOnebest();
			logger.info("onebest转写结果: " + onebestResult);
			//String simpleResult2 = voiceResult.getSimple2();
			//logger.info("简单转写结果2: " + simpleResult2);
			return onebestResult;
			
		}catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

}
