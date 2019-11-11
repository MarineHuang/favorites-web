package com.favorites.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.favorites.comm.aop.LoggerManage;
import com.favorites.domain.Favorites;
import com.favorites.domain.enums.IsDelete;
import com.favorites.domain.view.CollectSummary;
import com.favorites.repository.UserRepository;
import com.favorites.utils.DateUtils;
import com.favorites.utils.HtmlUtil;
import com.sqss.voice.entity.Audio;
import com.sqss.voice.repository.AudioRepository;

@Controller
@RequestMapping("/audio")
public class AudioController extends BaseController implements ApplicationRunner{
	@Autowired
	private AudioRepository audioRepository;
	
	//Save the uploaded file to this folder
	@Value("${voice.upload.folder}")
    private String VOICE_UPLOADED_FOLDER;
	
	 @Override
    public void run(ApplicationArguments args) throws Exception {
    	 File file = new File(VOICE_UPLOADED_FOLDER);
         if (!file.exists()) {
             file.mkdirs();
             logger.info("making directory: " + VOICE_UPLOADED_FOLDER);
         }
    }
	
    @RequestMapping(value="/list")
	@LoggerManage(description="录音转写")
	public String audioList(Model model) {
    	//Sort sort = new Sort(Sort.Direction.DESC, "id");
    	List<Audio> audiolist = audioRepository.findAll();
		model.addAttribute("size", audiolist.size());
		model.addAttribute("audiolist", audiolist);
		return "audio/audiolist";
	}
    
    /**
	 * 上传音频
	 *
	 */
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	@LoggerManage(description="上传音频操作")
	public String uploadAudio(Model model, @RequestParam("audioFile") MultipartFile audioFile){
		try {
			// Get the file and save it somewhere
            byte[] bytes = audioFile.getBytes();
            Path path = Paths.get(VOICE_UPLOADED_FOLDER + audioFile.getOriginalFilename());
            Files.write(path, bytes);
            
            Audio audio = new Audio(path.toString(), DateUtils.getCurrentTime());
            audioRepository.save(audio);
            
		} catch (Exception e) {
			logger.error("导入html异常:",e);
		}
		return "audio/audiolist";
	}
	
    /**
	 * 下载转写结果
	 *
	 */
	@RequestMapping(value="/download", method = RequestMethod.GET)
	@LoggerManage(description="下载转写结果操作")
	public String downLoad(@RequestParam("audioId") Long audioId, HttpServletResponse response){
		Audio audio = audioRepository.findById(audioId).get();
		String path = audio.getResultFullPath();
		File file = new File(path);
		String filename = file.getName();
		//判断文件父目录是否存在
		if(file.exists()){
			response.setHeader("content-type", "application/octet-stream");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment;filename=" + filename);
			
			byte[] buffer = new byte[1024];
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			OutputStream os = null;
			try {
			 // 获取响应对象的输出流
			 os = response.getOutputStream();
			 // 文件输入流
			 fis = new FileInputStream(file);
			 // 缓存输入流
			 bis = new BufferedInputStream(fis);
			 // 数组从输入流缓存读取数据,返回数据是读取的byte个数
			 int i = bis.read(buffer);
			 while(i != -1){
			     os.write(buffer, 0, i);
			     i = bis.read(buffer);
			 	}
			} catch (Exception e) {
			 e.printStackTrace();
			}
			try {
				 assert bis != null;
				 bis.close();
				 fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			logger.info("file not exits:" + path);
		}
		return null;
	}

}
