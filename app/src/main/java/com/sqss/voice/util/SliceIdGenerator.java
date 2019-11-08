package com.sqss.voice.util;

/**
 * ����slice_id�Ĺ�����
 * ÿ��תд�����½�һ��SliceIdGenerator�����շ�Ƭ˳����������slice_id
 * 
 * @author white
 *
 */
public class SliceIdGenerator {

    private static final String INIT_STR = "aaaaaaaaa`";
	private int length = 0;
	private char[] ch;

	public SliceIdGenerator() {
		this.length = INIT_STR.length();
		this.ch = INIT_STR.toCharArray();
	}

	/**
	 * ��ȡsliceId
	 * 
	 * @return
	 */
	public String getNextSliceId() {
		for (int i = 0, j = length - 1; i < length && j >= 0; i++) {
			if (ch[j] != 'z') {
				ch[j]++;
				break;
			} else {
				ch[j] = 'a';
				j--;
				continue;
			}
		}

		return new String(ch);
	}
}