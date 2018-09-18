package service.impl;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import dao.NoteDao;
import dao.NotebookDao;
import entity.Note;
import entity.Notebook;
import service.NoteContentErrorException;
import service.NoteEditException;
import service.NoteNotFoundException;
import service.NoteService;
import service.NotebookNotFoundException;
import service.UserNotFoundException;
import util.ServerFileAccessUtil;

@Controller("noteService")
public class NoteServiceImpl implements NoteService {

    public NoteServiceImpl() {

    }

    @Resource
    private NoteDao noteDao;
    @Resource
    private NotebookDao notebookDao;
    private ServerFileAccessUtil fileaccess;
    public static final int MAX_BACKUP = 3;
    
    /**
     * 根据笔记本的id查询所有笔记
     *
     * @param notebookId
     * @return
     * @throws NotebookNotFoundException
     */
    @Override
    public List<Map<String, Object>> listNotes(String notebookId) throws NotebookNotFoundException {
        if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("notebook的Id为空");
        }
        Notebook notebook = notebookDao.findNotebookByNotebookId(notebookId);
        if (notebook == null) {
            throw new NotebookNotFoundException("找不到对应的Notebook");
        }
        List<Map<String,Object>> noteList=noteDao.findNotesByNoteBookId(notebookId);
        for(Map<String,Object> map:noteList){
        	try {
				map.put("body", ServerFileAccessUtil.getNoteContentFromServer(map.get("id").toString(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return noteList;
    }

    /**
     * 查询所有笔记
     */
    @Override
    public List<Map<String, Object>> listAllNotes() {
    	List<Map<String,Object>> noteList=noteDao.findAllNotes();
    	for(Map<String,Object> map:noteList){
        	try {
				map.put("body", ServerFileAccessUtil.getNoteContentFromServer(map.get("id").toString(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return noteList;
    }

    /**
     * 根据笔记id查询笔记详细内容
     *
     * @param noteId
     * @return
     * @throws NoteNotFoundException
     */
    @Override
    public Map<String, Object> getNoteContent(String noteId) throws NoteNotFoundException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId不能为空");
        }
        Map<String ,Object> map = noteDao.findNoteByNoteId(noteId);
        try {
			map.put("body", ServerFileAccessUtil.getNoteContentFromServer(map.get("id").toString(), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        return map;
    }

    /**
     * 删除笔记,根据noteId
     *
     * @param noteId
     * @return
     * @throws NoteNotFoundException
     * @throws FileSystemException 
     */
    @Override
    public boolean deleteNote(String noteId) throws NoteNotFoundException, FileSystemException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId不能为空");
        }
        Map<String, Object> note = noteDao.findNoteByNoteId(noteId);
        if (note == null) {
            throw new NoteNotFoundException("笔记找不到");
        }
        int isDeletedDBRecord=noteDao.deleteNote(noteId);
        //删除掉对应笔记的内容文件
        boolean isDeleteLocalRecord=ServerFileAccessUtil.deleteContentFromServer(note.get("id").toString());
        if(isDeleteLocalRecord){
        	return isDeletedDBRecord==1;
        }
        return  false;
    }


    /**
     * 新增笔记
     *
     * @param notebookId
     * @param userId
     * @param body
     * @param title
     * @return
     * @throws NotebookNotFoundException
     * @throws UserNotFoundException
     */
    @Override
    public boolean addNote(String notebookId, String userId, String body, String title) throws NotebookNotFoundException, UserNotFoundException {
        if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("notebookId为空");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("userId为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new NoteContentErrorException("title为空");
        }
        Note noteTemp = noteDao.findNoteByTitleAndNotebookId(title, notebookId);
        if (noteTemp != null) {
            throw new NoteContentErrorException("笔记标题重复");
        }
        //初始化一个note对象，然后映射到数据库表里
        Note note = new Note();
        String id = UUID.randomUUID().toString();
        Long createTime = System.currentTimeMillis();
        note.setModifyTime(createTime);
        note.setId(id);
        note.setBody(body);
        note.setNotebookId(notebookId);
        note.setStatusId("normal");
        note.setTypeId(null);
        note.setTitle(title);
        note.setUserId(userId);
        note.setCreateTime(createTime);
        ServerFileAccessUtil.setNoteContentToServer(id, "UTF-8", body);
        int n = noteDao.addNote(note);
        return n == 1;
    }

   /**
     * 获取分享的笔记
     *
     * @param noteId
     * @param notebookId
     * @param userId
     * @return
     * @throws NotebookNotFoundException
     * @throws UserNotFoundException
     * @throws NoteNotFoundException
 * @throws IOException
     */
    @Override
    public boolean getSharedNote(String noteId, String notebookId, String userId) throws NotebookNotFoundException, UserNotFoundException, NoteNotFoundException, IOException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NotebookNotFoundException("noteId为空");
        }
        //返回的时查询到的note的信息
        Map<String, Object> map = getNoteContent(noteId);
        if (map == null || map.isEmpty()) {
            throw new NoteNotFoundException("没有对应的笔记");
        }
        String title = (String) map.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new NoteEditException("标题不能为空");
        }
        Note originNote = noteDao.findNoteByTitleAndNotebookId(title, notebookId);
        if (originNote != null)
            throw new NoteContentErrorException("笔记标题重复");
        
        String body=(String)map.get("body");
        boolean isDone = addNote(notebookId,userId,body,title);
        return isDone;
    }


    /**
     * 更新笔记，修改笔记内容
     *
     * @param body
     * @param title
     * @return
     * @throws NotebookNotFoundException
     * @throws UserNotFoundException
     * @throws IOException 
     */
    @Override
    public boolean updateNote(@RequestParam(required = true,value="noteId")String noteId, @RequestParam(required = true,value="notebookId")String notebookId, @RequestParam(required = true,value="title")String title, @RequestParam(required = true,value="body")String body) throws NotebookNotFoundException, UserNotFoundException, IOException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NotebookNotFoundException("noteId为空");
        }
        //返回的时查询到的note的信息
        Map<String, Object> map = noteDao.findNoteByNoteId(noteId);
        if (map == null || map.isEmpty()) {
            throw new NoteNotFoundException("没有对应的笔记");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new NoteEditException("标题不能为空");
        }
        String originId = noteDao.findNoteByTitleAndNotebookId(map.get("title").toString(), notebookId).getId();
        if (!originId.equals(noteId)) {
            throw new NoteContentErrorException("笔记标题重复");
        }
        Note noteUpdated = new Note();
        noteUpdated.setId(noteId);
        //如果从页面获取的title和原来的title不一样的话就更新数据库中的title
        if (!title.equals(map.get("title"))) {
            noteUpdated.setTitle(title);
        }
        //如果从页面获取的body和原来的body不一样的话就更新数据库中的body
        boolean isDone = true;
        if (body != null && !body.equals(ServerFileAccessUtil.getNoteContentFromServer(noteId, "UTF-8"))) {
        	isDone=ServerFileAccessUtil.setNoteContentToServer(noteId, "UTF-8", body);
        }
        //设置最后修改时间
        Long modifyTime = System.currentTimeMillis();
        noteUpdated.setModifyTime(modifyTime);
        int n = noteDao.updateNote(noteUpdated);
        if(isDone){
        	return n == 1;
        }
        return false;
    }


    @Override
    public boolean trashNote(String noteId, String statusId) throws NoteNotFoundException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId为空");
        }
        Map<String, Object> note = noteDao.findNoteByNoteId(noteId);
        if (note == null) {
            throw new NoteNotFoundException("笔记不存在");
        }
        Note noteUpdated = new Note();
        noteUpdated.setStatusId(statusId);
        noteUpdated.setId(noteId);
        int n = noteDao.updateNote(noteUpdated);
        return n == 1;
    }


    /**
     * 移动笔记时用的，把笔记的笔记本id给改了
     *
     * @param noteId
     * @param notebookId
     * @return
     * @throws NotebookNotFoundException
     * @throws NoteNotFoundException
     */
    @Override
    public boolean updateNote(String noteId, String notebookId, String title) throws NotebookNotFoundException, NoteNotFoundException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId为空");
        }
        if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("notebookId为空");
        }
        Notebook notebook = notebookDao.findNotebookByNotebookId(notebookId);
        if (notebook == null) {
            throw new NotebookNotFoundException("目标笔记本不存在");
        }
        Map<String, Object> map = noteDao.findNoteByNoteId(noteId);
        if (map == null || map.isEmpty()) {
            throw new NoteNotFoundException("没有对应的笔记");
        }
        Note oriNote = noteDao.findNoteByTitleAndNotebookId(title, notebookId);
        if (oriNote != null && !oriNote.getId().equals(noteId)) {
            throw new NoteContentErrorException("笔记标题重复");
        }
        Note noteUpdated = new Note();
        noteUpdated.setId(noteId);
        noteUpdated.setNotebookId(notebookId);
        noteUpdated.setModifyTime(System.currentTimeMillis());
        int note = noteDao.updateNote(noteUpdated);
        return note == 1;
    }

    /**
     * 回退笔记
     * @param noteId
	 * @param encoding
	 * @param content
	 * @return
	 * @throws IOException
	 */
    @SuppressWarnings("static-access")
	@Override
    public boolean backNote(String noteId,int backid) throws NoteNotFoundException, IOException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId不能为空");
        }
        Map<String, Object> note = noteDao.findNoteByNoteId(noteId);
        if (note == null) {
            throw new NoteNotFoundException("笔记找不到");
        }
        if ((int)note.get("back")== 0) {
            throw new NoteNotFoundException("无历史版本");
        }
        if(backid==0) {
        backid=(int)note.get("back");
        }
        String content=ServerFileAccessUtil.getBackNoteContentToServer(noteId, "utf-8", backid);
        fileaccess.setNoteContentToServer(noteId,"utf-8",content);
        return noteDao.backNote(noteId) == 1;
    }

    /**
     * 备份笔记
     * @param noteId
	 * @param encoding
	 * @param content
	 * @return
	 * @throws IOException
	 */
    @SuppressWarnings("static-access")
    @Override
    public boolean backupNote(String noteId) throws NoteNotFoundException, IOException {
        if (noteId == null || noteId.trim().isEmpty()) {
            throw new NoteNotFoundException("noteId不能为空");
        }
		Map<String, Object> note = noteDao.findNoteByNoteId(noteId);
        if (note == null) 
            throw new NoteNotFoundException("笔记找不到");
        int backid=(int) note.get("back");
        if((int)note.get("back")>=MAX_BACKUP){
        	for(int index=1;index<MAX_BACKUP;index++)
        		fileaccess.renameFile(noteId+"bak"+(index+1), noteId+"bak"+index);
        } else backid += 1;
        
        String content=fileaccess.getNoteContentFromServer(noteId, "utf-8");
        fileaccess.backupNoteContentToServer(noteId,"utf-8",backid,content);
        return noteDao.backupNote(noteId) == 1;
    }

    /** 
    * 上传文件
    * @param noteId
  	 * @param encoding
  	 * @param content
  	 * @return
  	 */
  	@Override
  	public boolean uploadFile(String notebookId, String userId,MultipartFile uploadFile) {
  		String id=UUID.randomUUID().toString();
  		if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("notebookId为空");
  		}
  		if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("userId为空");
  		}
  		if (uploadFile == null) {
            throw new NoteContentErrorException("文件未选择");
  		}
  		long  startTime=System.currentTimeMillis();
  		String originName=uploadFile.getOriginalFilename();
        Note oriNote = noteDao.findNoteByTitleAndNotebookId(originName, notebookId);
        if (oriNote != null) 
            throw new NoteContentErrorException("笔记标题重复");
        
  		ServerFileAccessUtil.tranToFile(id, uploadFile);
  		Note note=new Note();
  		note.setUserId(userId);
  		note.setNotebookId(notebookId);
  		note.setModifyTime(startTime);
  		note.setId(id);
  		note.setTitle(originName);
  		note.setCreateTime(startTime);
  		note.setStatusId("normal");
  		return noteDao.uploadFile(note) == 1;
  	}
      
      /** 
       * 下载文件
       * @param noteId
  	 * @param encoding
  	 * @param content
  	 * @return
       * @throws IOException 
  	 */
  	public ResponseEntity<byte[]> downloadFile(String noteId) throws IOException
  	{
  		Map<String, Object> note=noteDao.findNoteByNoteId(noteId);
		HttpHeaders headers = new HttpHeaders();  
		//下载显示的文件名，解决中文名称乱码问题  
		String downloadFielName = new String(((String) note.get("title")).getBytes("UTF-8"),"iso-8859-1");
		//通知浏览器以attachment（下载方式）打开图片
		headers.setContentDispositionFormData("attachment", downloadFielName); 
		//application/octet-stream ： 二进制流数据（最常见的文件下载）。
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(ServerFileAccessUtil.fetchFile((String)note.get("id")),    
			headers, HttpStatus.CREATED);  
  	}

}
