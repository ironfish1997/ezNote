package dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import entity.Notebook;

@Repository("notebookDao")
public interface NotebookDao {
    List<Map<String,Object>> findNotebooksByUserId(String userId);
    
    List<Map<String,Object>>  findAllNotebooksByUserId(String userId);
    
    Notebook findNotebookByNotebookId(String notebookId);

    Notebook findNotebookByName(String notebookName);

    int addNotebook(@Param("notebook") Notebook notebook);

    int deleteNotebook(String notebookId);

    int updateNotebook(@Param("notebook")Notebook nb);
    
}
