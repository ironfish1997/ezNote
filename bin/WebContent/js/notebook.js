/**
 * Div 元素
 */
//!notebook_id!:笔记本的id
//!child_notebook_div!:子笔记本div的集合
//!parent_id!:上一级的笔记本的id
let modalNotebookDiv = '<div class="card">' +
    '<div class="card-header" role="tab">' +
    '<a class="waves-effect btn_notebook" style="float: left; width: 80%" id="!notebook_id!" ' +
    'onclick="cur_notebook=\'!notebook_id!\';loadNotesByNotebook(\'!notebook_id!\');" ' +
    'oncontextmenu="cur_notebook=\'!notebook_id!\'">' +
    '<i class="fa fa-book"></i>' +
    '!notebook_name!' +
    '</a>' +
    '<a class="collapsed waves-effect" data-toggle="collapse" href="#!notebook_id!_body" aria-expanded="false" aria-controls="collapseThree">' +
    '<i class="fa fa-angle-down rotate-icon"></i>' +
    '</a>' +
    '</div>' +
    '<div id="!notebook_id!_body" class="collapse" role="tabpanel" aria-labelledby="!notebook_id!" data-parent="#!parent_id!_body_childList">' +
    '<div class="card-body">' +
    '<div class="accordion" id="!notebook_id!_body_childList" role="tablist" aria-multiselectable="true">' +
    '!child_notebook_div!' +
    '</div>' +
    '</div>' +
    '</div>' +
    '</div>';

// 叶子结点Div
let modelLeafNotebookDiv = '<div class="card">' +
    '<div class="card-header" role="tab">' +
    '<a class="waves-effect no-child btn_notebook" id="!notebook_id!" ' +
    'onclick="cur_notebook=\'!notebook_id!\';loadNotesByNotebook(\'!notebook_id!\');" ' +
    'oncontextmenu="cur_notebook=\'!notebook_id!\'">' +
    '<i class="fa fa-book"></i> !notebook_name!' +
    '</a></div></div>';

// 回收站Div
let modalTrashNotebookDiv = '<div class="card">' +
    '<div class="card-header" role="tab">' +
    '<a class="waves-effect text-center" style="padding: 0;"  onclick="cur_notebook=\'trash\';">' +
    '<i class="fa fa-trash"></i> 回收站' +
    '</a>' +
    '</div>' +
    '</div>';

///**
// * 把笔记本显示出来的li的模板
// */
//let showNotebookLiTemplate =
//    '<li class="online notebook">' +
//    '<a class="unchecked">' +
//    '<i class="fa fa-book" title="online" rel="tooltip-bottom"></i> [bookName] ' +
//    '<button type="button" class="btn btn-default btn-xs btn_position btn_delete">' +
//    '<i class="fa fa-times"></i></button>' +
//    '</a>' +
//    '</li>';

let cur_notebook = null;
let NB_MAP = {}; // Notebook map

$(function () {
    $.contextMenu({
        selector: '#root .btn_notebook',
        zIndex: 200,
        callback: function (key, options) {
            if (key === 'rename') {
            	showRenameNotebook();
            } else if (key === 'delete') {
                showDeleteConfirmNotebook();
            }
        },
        animation: {
            duration: 250,
            show: 'fadeIn',
            hide: 'fadeOut'
        },
        items: {
            "rename": {
                name: "重命名",
                icon: "edit",
            },
            "delete": {
                name: "删除",
                icon: "delete"
            }
        }
    });

    //网页加载以后，立即读取笔记本列表
    loadNoteBooks();
});


function showRenameNotebook(){
    $('#inputModalEditNotebook').val(NB_MAP[cur_notebook]);
	$('#modalEditNotebook').modal('show');
}

function loadNoteBooks() {
    //利用ajax从服务器获取数据
    let url = 'notebook/list.do';
    let data = {userId: getCookie('userId')};
    $.getJSON(url, data, function (result) {
        if (SUCCESS === result.state) {
            let notebook_data = result.data;
            // 在showNotebooks方法里
            // 把所有得到的notebooks显示到页面notebook-list区域
            let notebooks = '';
            NB_MAP = {};
            let sidenav_notebook = $('#root');
            $('.notebookList .initialized').empty();
            $('#modalNewNotebook .notebookList .initialized').append('<option value="">默认位置</option>');
            sidenav_notebook.empty();
            for (let i = 0; i < notebook_data.length; i++) {
                notebooks = notebooks + recurLoadNotebooks(notebook_data[i]);
            }
            // notebooks += modalTrashNotebookDiv; TODO: Trashbin
            let mdb_select = $('.mdb-select');
            mdb_select.material_select('destroy');
            mdb_select.material_select();
            sidenav_notebook.append(notebooks);
        } else {
            return toastr.error(result.message);

        }
    });
}


function recurLoadNotebooks(notebook) {
    //如果该节点为叶子节点
    $('.notebookList .initialized').append('<option value="'+notebook.id+'">'+notebook.name+'</option>');
    NB_MAP[notebook.id] = notebook.name;
    if (notebook.nodes.length === 0) {
        let nodeDiv = modelLeafNotebookDiv;
        nodeDiv = nodeDiv.replace(new RegExp('!notebook_id!', "g"), notebook.id);
        nodeDiv = nodeDiv.replace(new RegExp('!parent_id!', "g"), notebook.cn_notebook_parent_id == null ? '' : notebook.cn_notebook_parent_id);
        nodeDiv = nodeDiv.replace(new RegExp('!notebook_name!', "g"), notebook.name);
        nodeDiv = nodeDiv.replace(new RegExp('!child_notebook_div!', "g"), '');
        return nodeDiv;
    } else {
        //这个是根据传入的笔记本节点生成树节点
        let nodeDiv = modalNotebookDiv;
        nodeDiv = nodeDiv.replace(new RegExp('!notebook_id!', "g"), notebook.id);
        nodeDiv = nodeDiv.replace(new RegExp('!parent_id!', "g"), notebook.cn_notebook_parent_id == null ? '' : notebook.cn_notebook_parent_id);
        nodeDiv = nodeDiv.replace(new RegExp('!notebook_name!', "g"), notebook.name);
        let temp = '';
        for (let i = 0; i < notebook.nodes.length; i++) {
            temp = temp + recurLoadNotebooks(notebook.nodes[i]);
        }
        nodeDiv = nodeDiv.replace(new RegExp('!child_notebook_div!', "g"), temp);
        return nodeDiv;
    }
}

/**
 * 新建笔记本
 */
function addNoteBook() {
    //获取到笔记本的名字和当前用户的id
    let notebookName = $('#newNotebookTitle').val().trim();
    let userId = getCookie('userId');
    let parentId = $('#newNotebookNotebook').val();
    //如果notebookid为空，则提示并返回
    if (notebookName == null || notebookName === '')
        return toastr.error("笔记本名称不能为空");
    if (userId == null || userId === '') {
        toastr.error("用户id不能为空");
        window.location.href = 'login.html';
    }
    //发送ajax请求，新建一个笔记本
    $.post(
        'notebook/addNotebook.do',
        {
            userId: userId,
            title: notebookName,
            parentId: parentId
        },
        function (result) {
            if (result.state === 0) {
                toastr.success("新建笔记本成功");
                loadNoteBooks();
                $('#modalNewNotebook').modal('hide');
                $('#newNotebookTitle').val('');
            } else
                return toastr.error(result.message);
        }
    )
}


/**
 *  执行笔记本的删除操作
 */
function deleteNotebook() {
    if (cur_notebook == null || cur_notebook === '')
        return toastr.error('未选中笔记本');

    let url = 'notebook/deleteNotebook.do';
    let data = {
        notebookId: cur_notebook
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                toastr.success('删除笔记本成功');
                $('#modalConfirmDeleteNotebook').modal('hide');
                loadNoteBooks();
            }
            else {
                toastr.error(result.message);
            }
        }
    )
}

/**
 * 修改笔记本名称
 */
function updateNoteBook(name) {
    //如果新名字为空，则弹窗警告并退出
    if (name == null || name.trim() === '') {
        toastr.error('请输入笔记本名');
        return;
    }
    //从当前选中的笔记本li上拿到绑定的notebookId，从cookie中拿到userId
    let notebookId = cur_notebook;
    let userId = getCookie('userId');
    if (notebookId == null || notebookId.trim() === '') {
        toastr.error('未选定笔记本');
        return;
    }
    if (userId == null || userId.trim() === '') {
        toastr.error('用户未登录');
        window.location.href = 'login.html';
        return;
    }
    //发起ajax请求，更改笔记本的名称
    let url = 'notebook/updateNotebook.do';
    let data = {
        notebookId: notebookId,
        userId: userId,
        name: name
    };
    $.post(
        url,
        data,
        function (result) {
            if (result.state === SUCCESS) {
                toastr.success('修改成功');
                $('#modalEditNotebook').modal('hide');
                loadNoteBooks();

            } else {
                toastr.error(result.message);

            }
        }
    )
}

