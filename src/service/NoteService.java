package service;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface NoteService {
    List<Map<String, Object>> listNotes(String notebookId) throws NotebookNotFoundException;

    List<Map<String, Object>> listAllNotes();

    boolean addNote(String notebookId, String userId, String body, String title) throws NotebookNotFoundException, UserNotFoundException;

    boolean getSharedNote(String noteId,String notebookId, String userId) throws NotebookNotFoundException, UserNotFoundException,NoteNotFoundException, IOException;

    //这个方法是修改笔记的内容和标题
    boolean updateNote(String noteId,String notebookId, String title, String bodye) throws NotebookNotFoundException, UserNotFoundException, IOException;

    //这个方法用来把笔记放进和拿出回收站
    boolean trashNote(String noteId,String statusId) throws NoteNotFoundException;

    //这个方法用来变更笔记所在的笔记本
    boolean updateNote(String noteId,String notebookId,String title) throws NotebookNotFoundException,NoteNotFoundException;

    //通过noteId得到note的信息
    Map<String, Object> getNoteContent(String noteId) throws NoteNotFoundException;

    //通过noteId定位到相应的笔记并且删除它
    boolean deleteNote(String noteId) throws NoteNotFoundException, FileSystemException;

    boolean backNote(String noteId,int backid) throws NoteNotFoundException, IOException;

    boolean backupNote(String noteId) throws NoteNotFoundException, IOException;

    boolean uploadFile(String notebookId, String userId,MultipartFile uploadFile);

    ResponseEntity<byte[]> downloadFile(String noteId) throws IOException;
}
