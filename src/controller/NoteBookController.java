package controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import service.NoteBookService;
import service.NotebookNotFoundException;
import util.JsonResult;

@Controller("noteBookController")
@RequestMapping("/notebook")
public class NoteBookController extends AbstractController {

    public NoteBookController() {

    }

    @Resource
    private NoteBookService noteBookService;

    @RequestMapping("/list.do")
    @ResponseBody
    public JsonResult list(String userId) {
        List<Map<String, Object>> list = noteBookService.listNoteBooksByUserId(userId);
        return new JsonResult(list);
    }
    
    @RequestMapping("/listAll.do")
    @ResponseBody
    public JsonResult listAll(String userId){
    	 List<Map<String,Object>> list = noteBookService.listAllNotebooksByUserId(userId);
         return new JsonResult(list);
    }
    
    @RequestMapping("/addNotebook.do")
    @ResponseBody
    public JsonResult add(String title, String userId,String parentId) {
        Object isSuccess = noteBookService.addNotebook(title, userId,parentId);
        return new JsonResult(isSuccess);
    }

    @RequestMapping("/deleteNotebook.do")
    @ResponseBody
    public JsonResult delete(String notebookId) throws NotebookNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
    	
        Object isSuccess = noteBookService.deleteNotebook(notebookId);
        return new JsonResult(isSuccess);
    }

    @RequestMapping("/updateNotebook.do")
    @ResponseBody
    public JsonResult update(String notebookId, String userId, String name) {
        Object isSuccess = noteBookService.updateNotebook(notebookId, userId, name);
        return new JsonResult(isSuccess);
    }

}
