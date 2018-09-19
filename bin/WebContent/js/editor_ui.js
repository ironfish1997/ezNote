// Switch editor
function _disable_all(){
    document.getElementById("panelHtmlEditorLoader").style.display = "none";
    document.getElementById("panelMarkdownLoader").style.display = "none";
    document.getElementById("panelShowHtmlLoader").style.display = "none";
    document.getElementById("panelDownloadLoader").style.display = "none";
    document.getElementById("panelHelpLoader").style.display = "none";
}

function loadPanel(id) {
    _disable_all();
    document.getElementById("panel"+id+"Loader").style.display = "block";
}

function showInputBox(text, notice, title, btn_text){
    if (!text || !notice) return ;
    title = title || "请输入";
    btn_text = btn_text || "确认";
    $('#modalInputBox #inputBoxTitle').text(title);
    $('#modalInputBox #inputBoxText').text(text);
    $('#modalInputBox #inputBoxNotice').text(notice);
    $('#modalInputBox #inputBoxConfirm').text(btn_text);
    $('#modalInputBox').modal('show');
}

loadPanel("Help");

// 编辑网页中的面板控制
function showDeleteConfirmNotebook(){
    if (cur_notebook == null) return toastr.error('未选中笔记本');
    $('#modalConfirmDeleteNotebookTitle').html('<b>'+NB_MAP[cur_notebook]+'</b>');
    $('#modalConfirmDeleteNotebook').modal('show');
}
function showDeleteConfirmNote(){
    if (cur_note == null) return toastr.error('未选中笔记');
    $('#modalConfirmDeleteNoteTitle').html('<b>'+N_MAP[cur_note]+'</b>');
    $('#modalConfirmDeleteNote').modal('show');
}
