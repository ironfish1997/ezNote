package service;

import java.util.List;
import java.util.Map;

public interface NoteBookService {
    List<Map<String,Object>> listNoteBooksByUserId(String userId) throws UserNotFoundException;
    
    List<Map<String,Object>> listAllNotebooksByUserId(String userId);

    boolean deleteNotebook(String notebookId) throws NotebookNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException;

    boolean addNotebook(String notebookName,String userId, String parentId) throws UserNotFoundException;

    boolean updateNotebook(String notebookId, String userId, String name) throws NotebookNotFoundException,UserNotFoundException;
}
