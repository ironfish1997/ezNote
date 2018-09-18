/**
 * 需要解决的问题，笔记本不能重名，同一个笔记本里的笔记不能重名
 * @type {number}
 */

let SUCCESS = 0;
let ERROR = 1;
let cur_note = null;
let N_MAP = {};


//------------------------------html模板------------------------------------------------------
/**
 * 备份文件
 */
function backupNote(){
    let noteId = cur_note;
    let url='note/backupNote.do';
    let data={noteId:noteId};
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            alert('备份成功');
        } else {
            alert('备份失败');
        }
    });
}
/**
 * 回退文件
 */
function backNote(){
    let noteId = cur_note;
    let url='note/backNote.do';
    let data={noteId:noteId};
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            alert('回退成功成功');
        } else {
            alert('回退失败失败');
        }
    });
}


/**
 * 下载文件
 */
function downloadFile(){
    window.location.href = "note/down.do?noteId="+cur_note;
}

/**
 * 上传文件
 */
let tmp = null;
function uploadFile(){
    let filename = $('#inputUploadFile')[0].files[0];
    let formdata = new FormData();
    let noteBookId=$('#uploadFileNotebook').val();
    formdata.append("filename",filename);
    formdata.append("noteBookId",noteBookId);
    formdata.append("userId",getCookie('userId'));
    let url='note/uploadFile.do';
    if(uploadFile != null && uploadFile !== ""){
        $.ajax({
            type:'POST',
            url:url,
            data:formdata,
            processData:false,
            contentType:false,
            mimeType:"multipart/form-data",
            success: function(result){
                result = JSON.parse(result);
                if (result.state === SUCCESS) {
                    toastr.success("上传成功");
                    $('#modalUploadFile').modal('hide');
                    $('#inputUploadFile').val('');
                }
                else toastr.error(result.message, "上传失败");
            }
        });
    }
}

//------------------------------html模板------------------------------------------------------

/**
 * 笔记列表的模板
 * @type {string}
 */
let noteTemplate =
    '<a class="list-group-item list-group-item-action flex-column align-items-start waves-effect"' +
    '       onclick="cur_note=\'!note_id!\';chooseNote();">\n' +
    '                        <div class="d-flex w-100 justify-content-between">\n' +
    '                            <h5 class="mb-1 yh_font">!note_name!</h5>\n' +
    '                            <small>!note_time!</small>\n' +
    '                        </div>\n' +
    '                        <small>!note_type!</small>\n' +
    '                    </a>';

/**
 * 文件拓展名相关函数
 */
function fileExtension(str){
    let pos = str.lastIndexOf('.');
    if (pos === -1) return null;
    return str.substring(pos+1, str.length);
}
function fileExtName(str){
    let ext = fileExtension(str);
    if (ext == null) return '未知文件类型';
    switch (ext) {
        case 'md': return 'Markdown 笔记';
        case 'html': return '文本笔记';
        case 'txt': return 'txt 文档';
        case 'pdf': return 'PDF 文档';
        case 'jpg': case 'png': case 'bmp': return '图片文件';
        case 'mp3': case 'flac': case 'ape': return '音频文件';
        case 'mp4': case 'mov': case 'flv': return '视频文件';
        default: return '未知文件类型';
    }
}
function fileName(str){
    let pos = str.lastIndexOf('.');
    if (pos === -1) return str;
    return str.substring(0, pos);
}

function fileTime(timestamp) {
    let minute = 1000 * 60;
    let hour = minute * 60;
    let day = hour * 24;
    let month = day * 30;
    let now = new Date().getTime();
    let diffValue = now - timestamp;
    if (diffValue < 0) return '时间戳出错';
    let monthC = (diffValue / month>>0);
    let weekC = (diffValue / (7 * day)>>0);
    let dayC = (diffValue / day>>0);
    let hourC = (diffValue / hour>>0);
    let minC = (diffValue / minute>>0);
    if (monthC >= 1) return "" + monthC + "月前创建";
    else if (weekC >= 1) return "" + weekC + "周前创建";
    else if (dayC >= 1) return "" + dayC + "天前创建";
    else if (hourC >= 1) return "" + hourC + "小时前创建";
    else if (minC >= 1) return "" + minC + "分钟前创建";
    else return "刚刚创建";
}

//-----------------------有关笔记的函数-----------------------------------
/**
 * 利用notebookId来加载笔记本下所有笔记
 * @param notebookId
 * @param isShowNotes
 */
function loadNotesByNotebook(notebookId, isShowNotes) {
    //找到当前被选中的笔记本li
	N_MAP = {};
    let url = 'note/list.do';
    let data = {notebookId: notebookId};
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            let notes = result.data;
            $('#modalNotesTitle').html(NB_MAP[notebookId]);
            loopAddNotes(notes, isShowNotes);
        } else {
            toastr.error(result.message);
        }
    });
}

/**
 * 把笔记列表显示在屏幕上
 * @param notes
 * @param isShowNotes
 */
function loopAddNotes(notes, isShowNotes){
    isShowNotes = isShowNotes == null ? true : isShowNotes;
    let ul = $('#modalNotes .list-group');
    ul.empty();
    for (let i = 0; i < notes.length; i++) {
        let li = noteTemplate.replace('!note_name!', notes[i].title).replace('!note_id!', notes[i].id).
            replace('!note_type!', fileExtName(notes[i].title)).replace('!note_time!', fileTime(notes[i].createTime));
        li = $(li);
        N_MAP[notes[i].id] = [notes[i].title];
        li.data('noteId', notes[i].id);
        li.data('noteExt', fileExtension(notes[i].title));
        li.data('noteName', fileName(notes[i].title));
        if (notes[i].statusId === 'normal') {
            ul.append(li);
        }
    }
    if (isShowNotes) $('#modalNotes').modal('show');
}

function shareNote(){
    toastr.info(
        '<div class="md-form input-group">\n' +
        '  <input type="text" class="form-control text-white" id="copyHidden" value="' + cur_note + '">\n' +
        '  <div class="input-group-append">\n' +
        '    <button class="btn btn-secondary btn-sm waves-effect m-sm-0" type="button" ' +
        '      onclick="$(\'#copyHidden\').select();document.execCommand(\'copy\');toastr.success(\'复制成功！\');"><i class="fa fa-copy"></i></button>\n' +
        '  </div>\n' +
        '</div>', '请将此分享码复制给其他人', {timeOut: 10000});
}

function jumpLoginPage(){
    setTimeout(function (){
        location.href = 'edit.html';
    }, 900);
}

function getShareNote(noteId,toNotebook){
	if(noteId==null||noteId.trim()==='')
        return toastr.error('分享码错误,请重新输入');
	if(toNotebook==null||toNotebook.trim()==='')
        return toastr.error('请选择笔记本');
	let userId=getCookie('userId');
	if(userId==null||userId.trim()==='') {
        toastr.error('您还未登录');
        jumpLoginPage();
    }
	let url='note/getSharedNote.do';
	let data={
			noteId:noteId,
			notebookId:toNotebook,
			userId:userId
	};
	$.post(url, data, function (result) {
        if (result.state === SUCCESS) {
        	toastr.success('添加分享成功');
            loadNotesByNotebook(notebookId);
        } else {
        	toastr.error(result.message);
        }
    });
}

/**
 * 在垃圾箱显示所有被删除的笔记
 */
function showTrashNotes() {
    let url = 'note/listAll.do';
    let data = {};
    let notes = {};
    $('#edit_note').hide();
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            notes = result.data;
            //把垃圾箱列表显示出来
            $('#note-list').hide();
            $('#trash-bin').show();
            //找到显示列表的ul
            let ul = $('#trash-bin ul');
            ul.empty();
            //遍历笔记列表，将为每个对象创建一个li元素，添加到ul中
            for (let i = 0; i < notes.length; i++) {

                let li = trashTemplate.replace('[notetitle]', notes[i].title);
                li = $(li);
                //将noteId绑定到li
                li.data('noteId', notes[i].id);
                //如果标志位是delete证明这个笔记应该显示在垃圾箱
                if (notes[i].statusId === 'delete') {
                    ul.append(li);
                }
            }
        } else {
            toastr.error(result.message);
        }
    });
}

/**
 * 高亮显示当前选中的笔记li，并在编辑区和预览区显示笔记内容
 */
function chooseNote(){
    let url = 'note/getNoteContent.do';
    let data = {noteId: cur_note};
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            let data = result.data;
            switch (fileExtension(data.title)) {
                case 'md':
                    $('#inputMarkdownTitle').attr("value", fileName(data.title));
                    mdEditor.value(data.body);
                    if (!mdEditor.isPreviewActive())
                        mdEditor.togglePreview();
                    loadPanel('Markdown');
                    break;
                case 'html':
                    $('#inputHtmlTitle').attr("value", fileName(data.title));
                    $('#myEditor').data('editor').txt.html(data.body);
                    $('#panelShowHtmlTitle').html('查看笔记 - ' + data.title);
                    $('#panelShowHtmlBody').html(data.body);
                    loadPanel('ShowHtml');
                    break;
                default:
                    toastr.info('暂不支持该类型文件预览'); // TODO:
                    loadPanel('Download');
                    break;
            }
        }
    })
}

/**
 * 添加笔记
 */
function addNewNote(type){
    if (type !== 'md' && type !== 'html')
        return toastr.error('笔记类型出错');
    let title =  $(type === 'md' ? '#newMarkdownTitle' : '#newHtmlTitle').val();
    let userId = getCookie('userId');
    let notebookId = $(type === 'md' ? '#newMarkdownNotebook' : '#newHtmlNotebook').val();
    if (title === null || title === '')
        return toastr.error("笔记标题不能为空");
    if (notebookId === null || notebookId === '')
        return toastr.error("请选择笔记本");
    if (userId === null || userId === '') {
    	toastr.error("userId不能为空");
    	jumpLoginPage();
    }
    let url = "note/addNote.do";
    let data = {notebookId: notebookId, userId: userId, title: title+'.'+type, body: null};
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            toastr.success("笔记创建成功");
            $(type === 'md' ? '#newMarkdownTitle' : '#newHtmlTitle').val('');
            $(type === 'md' ? '#modalNewMarkdown' : '#modalNewHtml').modal('hide');
            loadPanel('Help');
            loadNotesByNotebook(notebookId);
        } else {
            return toastr.error(result.message);
        }
    });
}

/**
 * 修改笔记，点击保存修改按钮执行
 */
function updateNote(type) {
    if (type !== 'md' && type !== 'html')
        return toastr.error('笔记类型出错');
    let title = null, body = null;
    if (type === 'html'){
        //得到笔记编辑区里的标题
        title = $('#inputHtmlTitle').val().trim();
        //得到笔记编辑区的body
        let editor = $('#myEditor').data('editor');
        body = editor.txt.html();
    } else {
        title = $('#inputMarkdownTitle').val().trim();
        body = mdEditor.value();
    }
    //拿到当前选定的笔记li绑定的笔记id
    let noteId = cur_note;
//    if (noteId === null) {
//        noteId = $('#trash-bin .checked').parent().data('noteId');
//    }
    //得到选定的笔记本项的notebookId
    if (cur_notebook === null)
        return toastr.error("未选中笔记本");

    let data = {title: title+'.'+type, body: body, noteId: noteId, notebookId: cur_notebook};
    let url = 'note/updateNote.do';
    $.getJSON(url, data, function (result) {
        if (result.state === SUCCESS) {
            toastr.success("成功保存");
            loadPanel('Help');
            loadNotesByNotebook(cur_notebook, false);
        } else {
            toastr.error(result.message);
        }
    })
}

/**
 * 点击笔记下拉按钮,打开选项下拉列表
 */
function handleNoteBottom() {
    //找到当前点击的按钮
    let btn = $(this);
    //如果当前是被选中的笔记项，就弹出菜单
    let noteItem = btn.parent('.checked').next();
    noteItem.toggle();
    //阻止点击事件继续向上冒泡
    return false;

}

/**
 * 这个函数用来打开预览界面并重新加载预览
 */
function openPreviewPanel() {
    $('#preview_note_panel').css('display', 'inline');
    $('#edit_note_panel').css('display', 'none');
}

/**
 * 这个函数用来打开编辑界面并加载内容
 */
function openEditPanel() {
    $('#preview_note_panel').css('display', 'none');
    $('#edit_note_panel').css('display', 'inline');
}

/**
 * 隐藏所有下拉菜单（待完成）
 */
function hideNoteMenu() {
    //隐藏所有的下拉笔记菜单
    $('.note_menu').hide();
}


/**
 * 显示移动笔记页面，并加载所有笔记本列表以供移动
 */
function getNotebookListInSelect() {
    $('#can').load('./alert/alert_move.html', function () {
        // 获取笔记本列表
        setNoteBookToSelect();
        //让光标保持在选项框里
        $('#moveSelect').focus();
    });
    $('.opacity_bg').show();
}

/**
 * 在移动笔记界面加载所有可供移入笔记本列表,从数据库加载笔记本数据
 */

function setNoteBookToSelect() {
    let userId = getCookie('userId');
    if (userId === null || userId === '') {
        toastr.error("userId不能为空");
        return;
    }
    let url = 'notebook/listAll.do';
    let data = {
        userId: userId
    };
    $.getJSON(url, data, function (result) {
        if (SUCCESS === result.state) {
            let notebooks = result.data;
            //在showMoveSelector函数里把所有拿到的笔记本名称显示出来
            showMoveSelector(notebooks);
        }
        else {
            toastr.error(result.message);
        }
    });
}

/**
 * 显示所有可供移动选择的笔记本选项
 * @param notebooks
 */
function showMoveSelector(notebooks) {
    //定位到select下拉框
    let select = $('#moveSelect');
    //清空所有下拉框选项
    select.empty();
    let optionTemplate = '<option>[value]</option>';
    for (let i = 0; i < notebooks.length; i++) {
        let option = optionTemplate.replace('[value]', notebooks[i].name);
        option = $(option);
        //将notebookId绑定到相应的选项上
        option.data('notebookId', notebooks[i].id);
        //把生成的选项加到selector下
        select.append(option);
    }
}

/**
 * 移动笔记
 */

function moveNote() {
    //拿到当前选中项
    let option = $('#moveSelect option:selected');
    let notebookId = option.data('notebookId');
    let noteLi = $('#note-show-ul .online .checked').parent();
    let noteId = noteLi.data('noteId');
    let title = $('#note_title_content').text();
    //如果拿到的notebookId为空则提示
    if (noteId === null || notebookId === '') {
        toastr.error('笔记id为空');
        return;
    }
    let url = 'note/moveNote.do';
    let data = {
        notebookId: notebookId,
        noteId: noteId,
        title: title
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                toastr.success('移动笔记成功');
                //触发一次笔记本点击，重新加载笔记
                $('#first_side_right .node-selected').click();
                $('#first_side_right li:eq(0)').click();
            } else {
                toastr.error(result.message);
            }
        }
    )
}

/**
 * 把笔记的标志位从normal改为delete，把笔记放进垃圾箱
 */
function trashNote() {
    //从当前选中的笔记li上拿到绑定的noteId
    let noteId = $('#note-list .checked').parent().data('noteId');
    //检查noteId是否拿到了，如果没拿到直接弹窗
    if (noteId === null || noteId === '') {
        toastr.error("未选定笔记");
        return;
    }
    let url = 'note/trashNote.do';
    let data = {
        noteId: noteId,
        statusId: 'delete'
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                toastr.success('放入回收站成功');
              //触发一次笔记本点击，重新加载笔记
                $('#first_side_right .node-selected').click();
                $('#first_side_right li:eq(0)').click();
            } else {
                toastr.error(result.message);
            }
        }
    )

}

/**
 * 根据得到的noteId删除笔记,这个函数应该是在垃圾箱界面被调用
 */
function deleteNote() {
    if (cur_note === null || cur_note === '') {
        toastr.error("未选定笔记");
        return;
    }
    let url = 'note/deleteNote.do';
    let data = {
        noteId: cur_note
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                toastr.success('删除成功');
                loadPanel('Help');
                $('#modalConfirmDeleteNote').modal('hide');
            } else {
                toastr.error(result.message);
            }
        }
    )

}

/**
 * 显示回收站内笔记移动页面，并加载所有笔记本列表以供移动
 */
function getNotebookListInTrashSelect() {
    $('#can').load('./alert/alert_replay.html', function () {
        // 获取笔记本列表
        setNoteBookToTrashSelect();
        //让光标保持在选项框里
        $('#replaySelect').focus();
    });
    $('.opacity_bg').show();
}

/**
 * 在移动垃圾箱笔记界面加载所有可供移入笔记本列表,从数据库加载笔记本数据
 */

function setNoteBookToTrashSelect() {
    let userId = getCookie('userId');
    if (userId === null || userId === '') {
        toastr.error("userId不能为空");
        return;
    }
    let url = 'notebook/list.do';
    let data = {
        userId: userId
    };
    $.getJSON(url, data, function (result) {
        if (SUCCESS === result.state) {
            let notebooks = result.data;
            //在showMoveSelector函数里把所有拿到的笔记本名称显示出来
            showTrashMoveSelector(notebooks);
        }
        else {
            toastr.error(result.message);
        }
    });
}

/**
 * 显示所有可供移动选择的笔记本选项
 * @param notebooks
 */
function showTrashMoveSelector(notebooks) {
    //定位到select下拉框
    let select = $('#replaySelect');
    //清空所有下拉框选项
    select.empty();
    let optionTemplate = '<option>[value]</option>';
    for (let i = 0; i < notebooks.length; i++) {
        let option = optionTemplate.replace('[value]', notebooks[i].name);
        option = $(option);
        //将notebookId绑定到相应的选项上
        option.data('notebookId', notebooks[i].id);
        //把生成的选项加到selector下
        select.append(option);
    }
}


/**
 * 恢复在回收站里的笔记
 */
function moveTrash() {
    //拿到当前选中项
    let option = $('#replaySelect option:selected');
    let notebookId = option.data('notebookId');
    let noteLi = $('#trash_show_li .checked').parent();
    let noteId = noteLi.data('noteId');
    let title = $('#note_title_content').text();
    //如果拿到的notebookId为空则提示
    if (noteId === null || notebookId === '') {
        toastr.error('笔记id为空');
        return;
    }
    let url = 'note/moveNote.do';
    let data = {
        notebookId: notebookId,
        noteId: noteId,
        title: title
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                $.post(
                    'note/trashNote.do',
                    {
                        noteId: noteId,
                        statusId: 'normal'
                    },
                    function (result) {
                        if (result.state === SUCCESS) {
                            toastr.success('恢复笔记成功');
                            //触发一次回收站按钮点击事件，重新加载回收站笔记
                            $('#rollback_button').click();
                        } else {
                            toastr.error('恢复笔记失败,请重试');
                        }
                    }
                );

            } else {
                toastr.error(result.message);
            }
        }
    )
}