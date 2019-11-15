package com.sqss.voice.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class IflytekVoiceResult {
	private List<Sent> sentsResultList = new ArrayList<Sent>();
	
	public IflytekVoiceResult(String resultStr) {
		try {
			sentsResultList = JSONObject.parseArray(resultStr, Sent.class);
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
	}
	
	public String getOnebest() {
        String str="";
        for(Sent s : sentsResultList){
        	str += s.getOnebest();
        }
        return str;
    }
	
	public String getSimple2() {
        String str="";
        for(Sent s : sentsResultList){
        	for(Word w : s.getWordsResultList()) {
        		str += w.getWordsName();
        	}
        }
        return str;
    } 
}

class Sent{
	private Long bg;
	private Long ed;
	private String onebest;
	private Long si;
	private int speaker;
	private List<Word> wordsResultList  = new ArrayList<Word>();
	
	public Long getBg() {
		return bg;
	}
	public void setBg(Long bg) {
		this.bg = bg;
	}
	public Long getEd() {
		return ed;
	}
	public void setEd(Long ed) {
		this.ed = ed;
	}
	public String getOnebest() {
		return onebest;
	}
	public void setOnebest(String onebest) {
		this.onebest = onebest;
	}
	public Long getSi() {
		return si;
	}
	public void setSi(Long si) {
		this.si = si;
	}
	public int getSpeaker() {
		return speaker;
	}
	public void setSpeaker(int speaker) {
		this.speaker = speaker;
	}
	public List<Word> getWordsResultList() {
		return wordsResultList;
	}
	public void setWordsResultList(List<Word> wordsResultList) {
		this.wordsResultList = wordsResultList;
	}
	
}

class Word {
	private List<String> alternativeList = new ArrayList<String>();
	private float wc;
	private Long wordBg;
	private Long wordEd;
	private String wordsName;
	private String wp;
	public float getWc() {
		return wc;
	}
	public void setWc(float wc) {
		this.wc = wc;
	}
	public Long getWordBg() {
		return wordBg;
	}
	public void setWordBg(Long wordBg) {
		this.wordBg = wordBg;
	}
	public Long getWordEd() {
		return wordEd;
	}
	public void setWordEd(Long wordEd) {
		this.wordEd = wordEd;
	}
	public String getWordsName() {
		return wordsName;
	}
	public void setWordsName(String wordsName) {
		this.wordsName = wordsName;
	}
	public String getWp() {
		return wp;
	}
	public void setWp(String wp) {
		this.wp = wp;
	}
	public List<String> getAlternativeList() {
		return alternativeList;
	}
	public void setAlternativeList(List<String> alternativeList) {
		this.alternativeList = alternativeList;
	}
	
}