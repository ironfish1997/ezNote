package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import service.NoteNotFoundException;
import service.NoteService;
import service.NotebookNotFoundException;
import service.UserNotFoundException;
import util.JsonResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;

@Controller("noteController")
@RequestMapping("/note")
public class NoteController extends AbstractController {

    public NoteController() {

    }

    @Resource
    private NoteService noteService;

    @RequestMapping("/list.do")
    @ResponseBody
    public Object list(String notebookId) {
        List<Map<String, Object>> list = noteService.listNotes(notebookId);
        return new JsonResult(list);
    }

    @RequestMapping("/listAll.do")
    @ResponseBody
    public Object listAll() {
        List<Map<String, Object>> list = noteService.listAllNotes();
        return new JsonResult(list);
    }

    @RequestMapping("/addNote.do")
    @ResponseBody
    public Object addNote(String notebookId, String userId, String title, String body) {
        return new JsonResult(noteService.addNote(notebookId, userId, body, title));
    }

    @RequestMapping("/getSharedNote.do")
    @ResponseBody
    public Object getSharedNote(String noteId,String notebookId, String userId) throws NotebookNotFoundException, UserNotFoundException, NoteNotFoundException, IOException {
        return new JsonResult(noteService.getSharedNote(noteId,notebookId, userId));
    }
    @RequestMapping("/updateNote.do")
    @ResponseBody
    public JsonResult updateNote(String noteId, String notebookId, String title, String body) throws NotebookNotFoundException, UserNotFoundException, IOException {
        return new JsonResult(noteService.updateNote(noteId, notebookId, title, body));
    }

    @RequestMapping("/moveNote.do")
    @ResponseBody
    public JsonResult moveNote(String noteId, String notebookId, String title) {
        return new JsonResult(noteService.updateNote(noteId, notebookId, title));
    }

    @RequestMapping("/getNoteContent.do")
    @ResponseBody
    public JsonResult getNoteContentByNoteId(String noteId) {
        return new JsonResult(noteService.getNoteContent(noteId));
    }
    
    @RequestMapping("/trashNote.do")
    @ResponseBody
    public JsonResult trashNote(String noteId, String statusId) {
        return new JsonResult(noteService.trashNote(noteId, statusId));
    }

    @RequestMapping("/deleteNote.do")
    @ResponseBody
    public JsonResult deleteNote(String noteId) throws NoteNotFoundException, FileSystemException {
        return new JsonResult(noteService.deleteNote(noteId));
    }

    @RequestMapping("/backNote.do")
    @ResponseBody
    public JsonResult backNote(String noteId,int backid) throws NoteNotFoundException, IOException {
        return new JsonResult(noteService.backNote(noteId,backid));
    }

    @RequestMapping("/backupNote.do")
    @ResponseBody
    public JsonResult backupsNote(String noteId) throws NoteNotFoundException, IOException {
        return new JsonResult(noteService.backupNote(noteId));
    }

    @RequestMapping("/uploadFile.do")
    @ResponseBody
    public JsonResult uploadNote(HttpServletRequest request,
			@RequestParam MultipartFile filename) {
    	String noteBookId = request.getParameter("noteBookId");
    	String userId=request.getParameter("userId");
        return new JsonResult(noteService.uploadFile(noteBookId,userId,filename));
    }

    @RequestMapping("/down.do")
    @ResponseBody
    public ResponseEntity<byte[]> down(HttpServletRequest request,HttpServletResponse response) throws IOException{
    	String id=request.getParameter("noteId");
        return noteService.downloadFile(id);
    }
}
