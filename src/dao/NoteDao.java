package dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import entity.Note;

@Repository("noteDao")
public interface NoteDao {
    List<Map<String, Object>> findNotesByNoteBookId(String noteBookId);

    Map<String, Object> findNoteByNoteId(String noteId);

    List<Map<String, Object>> findAllNotes();

    Note findNoteByTitleAndNotebookId(String title,String notebookId);

    int addNote(@Param("note") Note note);

    int updateNote(@Param("note") Note note);

    int deleteNote(String noteId);

    int backNote(String noteId);

    int backupNote(String noteId);

    int uploadFile(@Param("note") Note note);
}
