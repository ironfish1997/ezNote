package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystemException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import service.NoteContentErrorException;

public class ServerFileAccessUtil {
	/**
	 * 配置文件映射表
	 */
	private static final Map<String, Object> propertyMap = new HashMap<>();

	static {
		Properties property = new Properties();
		try (InputStream is = ServerFileAccessUtil.class.getResourceAsStream("/conf/server.properties")){
			property.load(is);
			Iterator<String> it = property.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Object value = property.get(key);
				propertyMap.put(key, value);
			}
			String baseDir=(String) propertyMap.get("baseDir");
			File f=new File(baseDir + "data/");
			if(!f.exists()){
				f.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("文件服务器配置读取错误!");
		}
	}

	/**
	 * 通过笔记id获取笔记内容
	 * 
	 * @param noteId
	 *            笔记id
	 * @param encoding
	 *            字符编码
	 * @return
	 * @throws IOException
	 */
	public static String getNoteContentFromServer(String noteId, String encoding) throws IOException {
		String baseDir = (String) propertyMap.get("baseDir");
		String result = "";
		File file = new File(baseDir + "data/" + noteId);
		if (!file.exists()) {
			file.createNewFile();
		}
		try (BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))){
			StringBuffer sb = new StringBuffer();
			String s;
			while ((s = is.readLine()) != null) {
				sb.append(s + "\n");
			}
			result = sb.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 上传文件
	 * @param id
	 * @param uploadFile
	 */
	public static void tranToFile(String id,MultipartFile uploadFile) {
		String filePath=(String)propertyMap.get("baseDir") + "data/" + id;
  		if (!uploadFile.isEmpty()) {
  			try {			
  				// 转存文件
  				uploadFile.transferTo(new File(filePath));
  			} catch (Exception e) {
  				throw new NoteContentErrorException("文件未上传成功");
  			}
  		}
	}
	
	/**
	 * 上传头像
	 * @param id
	 * @param uploadFile
	 */
	public static void uploadAvatar(String userId,MultipartFile img) {
		String filePath=(String)propertyMap.get("baseDir") + "avatar/" + userId + ".png";
  		if (!img.isEmpty()) {
  			try {			
  				// 转存文件
  				img.transferTo(new File(filePath));
  			} catch (Exception e) {
  				throw new NoteContentErrorException("头像未上传成功");
  			}
  		}
	}
	
	/**
	 * 判断自定义头像存在
	 */
	public static boolean isAvatarExisted(String userId) {
		String filePath=(String)propertyMap.get("baseDir") + "avatar/" + userId + ".png";
		return new File(filePath).exists();
	}
	
	/**
	 * 获取文件
	 * @param id
	 * @throws IOException 
	 */
	public static byte[] fetchFile(String id) throws IOException {
		String filePath=(String)propertyMap.get("baseDir") + "data/" + id;
		File file = new File(filePath);
		return FileUtils.readFileToByteArray(file);
	}
	
	/**
	 * 覆盖指定笔记的内容,如果没有该笔记则创建该笔记
	 * 
	 * @param noteId
	 * @param encoding
	 * @param content
	 * @return
	 */
	public static boolean setNoteContentToServer(String noteId, String encoding, String content) {
		String baseDir = (String) propertyMap.get("baseDir");
		File file = new File(baseDir + "data/" + noteId);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding)))
		{
			if (!file.exists()) {
				file.createNewFile();
			}
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 删除笔记内容
	 * @param noteId
	 * @return
	 * @throws FileSystemException
	 */
	public static boolean deleteContentFromServer(String noteId) throws FileSystemException{
		String baseDir = (String) propertyMap.get("baseDir");
		File file=new File(baseDir + "data/" + noteId);
		if(!file.exists()){
			return true;
		}else{
			try{
				file.delete();
			}catch(Exception e){
				throw new FileSystemException("文件删除失败");
			}
			return true;
		}
	}

	/**
	 * 备份笔记
	 * @param noteId
	 * @param encoding
	 * @param content
	 * @return
	 */
	public static boolean backupNoteContentToServer(String noteId, String encoding, int noteBack,String content) {
		String baseDir = (String) propertyMap.get("baseDir");
		File file = new File(baseDir + "data/" + noteId+"bak"+noteBack);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 读取备份文件
	 * @param noteId
	 * @param encoding
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static String getBackNoteContentToServer(String noteId, String encoding, int noteBack) throws IOException {
		String baseDir = (String) propertyMap.get("baseDir");
		String result = "";
		File file=new File(baseDir + "data/" + noteId+"bak"+noteBack);
		if(!file.exists()){
			file.createNewFile();
		}
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader is = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), encoding));
			String s;
			while ((s = is.readLine()) != null) {
				sb.append(s + "\n");
			}
			result = sb.toString().trim();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 重命名文件
	 * @param sourceName
	 * @param targetName
	 * @return
	 */
	public static boolean renameFile(String sourceName, String targetName) {
		String baseDir = (String) propertyMap.get("baseDir");
		File source = new File(baseDir + "data/" + sourceName), target = new File(baseDir + "data/" + targetName);
		if (target.exists())
			target.delete();
		source.renameTo(target);
		return target.exists();
	}

	public static void main(String[] args) throws IOException {
		ServerFileAccessUtil.setNoteContentToServer("1233", "UTF-8", "刘利用");
		System.out.println(ServerFileAccessUtil.getNoteContentFromServer("1233", "UTF-8"));
	}
}
