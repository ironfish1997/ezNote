package service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.stereotype.Service;

import dao.NotebookDao;
import dao.UserDao;
import entity.Notebook;
import entity.User;
import service.NoteBookService;
import service.NotebookNotFoundException;
import service.UserNameException;
import service.UserNotFoundException;

@Service("noteBookService")
public class NoteBookServiceImpl implements NoteBookService {
    public NoteBookServiceImpl() {
    }

    @Resource
    private NotebookDao notebookDao;

    @Resource
    private UserDao userDao;

    @Override
    public List<Map<String,Object>> listNoteBooksByUserId(String userId) throws UserNotFoundException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("用户id为空");
        }
        User user = userDao.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }
        List<Map<String,Object>> result=notebookDao.findNotebooksByUserId(userId);
        parseList(result);
        return result;
    }
    
    protected void parseList(List<Map<String,Object>> list){
    	for(Map<String, Object> nb:list){
    		nb.put("text", nb.get("name"));
			nb.put("nodes", nb.get("childNoteBook"));
			nb.remove("childNoteBook");
    		//如果得到的节点没有子节点了，则处理内部值
    		if(nb.get("nodes")!=null){
    			@SuppressWarnings("unchecked")
				List<Map<String,Object>> nbl= (List<Map<String,Object>>) nb.get("nodes");
    			parseList(nbl);
    		}
    	}
    }

    @Override
    public boolean deleteNotebook(String notebookId) throws NotebookNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
        if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("笔记本id为空");
        }
        Notebook notebook = notebookDao.findNotebookByNotebookId(notebookId);
        if (notebook == null) {
            throw new NotebookNotFoundException("笔记本不存在");
        }
        try{
        	recursionDeleteChildNotebook(notebook);
        	}catch(Exception e){
        		return false;
        	}
        return true;
    }
    
    private final void recursionDeleteChildNotebook(Notebook notebook){
    	List<Notebook> temp=notebook.getChildNoteBook();
    	if(temp==null||temp.size()==0){
    		notebookDao.deleteNotebook(notebook.getId());
    	}else{
    		notebookDao.deleteNotebook(notebook.getId());
    		List<Notebook> lstemp=notebook.getChildNoteBook();
    		for(Notebook nb:lstemp){
    			recursionDeleteChildNotebook(nb);
    		}
    	}
    }

    @Override
    public boolean addNotebook(String notebookName, String userId, String parentId) throws UserNotFoundException {
        //如果没有从前端拿到user的id，则抛出错误
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("用户id为空");
        }
        if (notebookName == null || notebookName.trim().isEmpty()) {
            throw new NotebookNotFoundException("笔记本名称为空");
        }
        User user = userDao.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }

        Notebook temp = notebookDao.findNotebookByName(notebookName);
        if (temp != null) {
            throw new UserNameException("笔记本名称不能重复");
        }
        Notebook notebook = new Notebook();
        notebook.setId(UUID.randomUUID().toString());
        notebook.setName(notebookName);
        notebook.setUserId(userId);
        notebook.setCreateTime(new Timestamp(System.currentTimeMillis()));
        notebook.setParentId(parentId);
        return notebookDao.addNotebook(notebook) == 1;
    }

    /**
     * 这个方法用来修改笔记本的相关数据
     *
     * @throws NotebookNotFoundException
     * @throws UserNotFoundException
     */
    @Override
    public boolean updateNotebook(String notebookId, String userId, String name) throws NotebookNotFoundException, UserNotFoundException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("用户id为空");
        }
        if (notebookId == null || notebookId.trim().isEmpty()) {
            throw new NotebookNotFoundException("笔记本id为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new NotebookNotFoundException("笔记本名称为空");
        }
        if (userDao.findUserById(userId) == null) {
            throw new UserNotFoundException("找不到用户");
        }
        Notebook notebook = notebookDao.findNotebookByNotebookId(notebookId);
        if (notebook == null) {
            throw new NotebookNotFoundException("找不到笔记本");
        }
        if (notebookDao.findNotebookByName(name) != null) {
            throw new UserNameException("笔记本名称不能重复");
        }
        notebook.setId(notebookId);
        notebook.setUserId(userId);
        notebook.setName(name);
        int i = notebookDao.updateNotebook(notebook);
        return i == 1;
    }

	@Override
	public List<Map<String, Object>> listAllNotebooksByUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("用户id为空");
        }
        User user = userDao.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }
        List<Map<String,Object>> result=notebookDao.findAllNotebooksByUserId(userId);
        return result;// TODO Auto-generated method stub
	}
	
    @Test
    public void main() {
    	// TODO: Test
//		NoteBookServiceImpl im=new NoteBookServiceImpl();
//		List<Map<String,Object>> lis=im.listNoteBooksByUserId("1");
//		for(Map<String, Object> nb:lis){
//		}
	}
}
