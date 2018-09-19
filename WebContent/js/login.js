/**
* 使用到的全局变量
 */
let timeout = null; // 判断停止输入的计时器
const TIMEOUT = 500; // 半秒内无时间刷新，视为输入停止


/**
 * 工具函数
 */
// 绑定停止输入时执行函数（装饰器）
function checkOnKeyUp(func){
    return function (){
        clearTimeout(timeout);
        timeout = setTimeout(func, TIMEOUT);
    }
}
// 绑定常规按钮事件，弹起及失去焦点
function bindKeyUpAndBlur(elemId, func){
    let elem = $('#'+elemId);
    elem.bind('keyup', checkOnKeyUp(func));
    elem.bind('blur', func);
}
// UI错误提示，有相关err元素则修改，否则弹出提示信息
function invalid(inputElemId, info){
    let input = document.getElementById(inputElemId);
    let err = document.getElementById(inputElemId+'_err');
    if (input){
        input.classList.remove("valid");
        input.classList.add("invalid");
    }
    if (err){
        err.innerText = info;
        err.style.display = 'block';
    } else toastr.error(info);
    return false;
}
// UI正确提示，同上
function valid(inputElemId, info){
    let input = document.getElementById(inputElemId);
    let err = document.getElementById(inputElemId+'_err');
    if (input) {
        input.classList.remove("invalid");
        input.classList.add("valid");
    }
    if (err) err.style.display = 'none';
    if (info) toastr.success(info);
    return true;
}
// 正则验证（邮箱、昵称及密码）
function isValidEmail(email) {
    let re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}
function isValidNickname(nickname){
    let re = /^[\u4e00-\u9fa5]{2,10}|[\w ]{4,20}$/;  // 2-10个连续中文和字母数字或4-20个英文字母下划线或空格
    return re.test(nickname);
}
function isValidPassword(password){
    let re = /^\S{6,20}$/;  // 6-20个非空字符
    return re.test(password);
}
// TODO: 暂时删除账户名
// function isValidAccount(account){
//     let re = /^(?=[a-z_\d]*[a-z])[a-z_\d]{4,15}$/;  // 以字母开头，由字母数字下划线组成的4-15个字符
//     return re.test(account);
// }

// 请求判断邮箱是否存在
function isEmailRegistered(email){
    let isExisted = "ERR";
    $.ajax({
        url: 'user/existed.do',
        data: { "email": email },
        async: false,
        type: 'get',
        dataType: 'json',
        success: function (result) {
            if (result.state !== 0)
                toastr.error('未知错误：' + result.message);
            isExisted = result.data;
        },
        error: function () {
            toastr.error('通信失败，服务器没有响应');
        }
    });
    if (isExisted === "ERR")
        toastr.error('未知错误');
    return isExisted;
}


/**
 * 页面初始化后，为输入框绑定函数
 */
$(function () {
    // 登录和注册时的按键触发检查函数
    bindKeyUpAndBlur('log_email', checkLoginEmail);
    bindKeyUpAndBlur('log_pass', checkLoginPasswordEmpty);
    bindKeyUpAndBlur('reg_email', checkRegEmail);
    bindKeyUpAndBlur('reg_nick', checkRegNickname);
    bindKeyUpAndBlur('reg_pass', checkRegPassword);
    bindKeyUpAndBlur('reg_pass_con', checkRegPasswordConfirm);
});


/**
 * 登录功能验证
 */
// 检验登录邮箱是否符合要求
function checkLoginEmail() {
    let email = document.getElementById("log_email").value;
    if (email.length === 0) return invalid("log_email", "邮箱不能为空");
    if (!isValidEmail(email)) return invalid("log_email", "邮箱格式不规范");
    if (!isEmailRegistered(email)) return invalid("log_email", "该邮箱未注册");
    return valid("log_email");
}

//检查cookie中是否有token，以及token是否过期
function checkIsAutoLogin() {
	let token = getCookie('token');
	if (token === null) {
		return;
	}
	let data = {
		'token' : token
	};
	$.post({
		url : 'user/checkAutoLogin.do',
		data : data,
		type : 'post',
		dataType : 'json',
		success : function(result) {
			if (result.state === 0) {
				// 登录成功
				let user = result.data;
				// 保存登录的userId到cookie
				addCookie("userId", user.id);
				addCookie("userEmail", user.email);
				addCookie("userNick", user.nick);
				addCookie("token", user.token)
				// 显示登录成功界面
				$('#modalLogin').modal('hide');
				$('#modalLoginSuccess').modal('show');
				location.href = 'edit.html';
			}
		}
	});
}

// 检验登录密码是否为空
function checkLoginPasswordEmpty() {
    let pass = document.getElementById("log_pass").value;
    if (pass.length === 0) return invalid("log_pass", "密码不能为空");
    return valid("log_pass");
}

// 提交登录表单
function submitInputForm() {
    if (!checkLoginEmail() || !checkLoginPasswordEmpty())
        return false;
    let email = $("#log_email").val(), pass = $("#log_pass").val();
    let expire = document.getElementById("log_expired_time").value === null ? 0
        : document.getElementById("log_expired_time").value;
    let expireTime = expire;
    let data = {
        "email": email,
        "password": pass,
        "expireTime" : expireTime
    };
    $.ajax({
        url: 'user/login.do',
        data: data,
        type: 'post',
        dataType: 'json',
        success: function (result) {
            if (result.state === 0) {
                //登录成功
                let user = result.data;
                //保存登录的userId到cookie
                addCookie("userId", user.id);
                addCookie("userEmail", user.email);
                addCookie("userNick",user.nick);
                addCookie("token", user.token)
                // 显示登录成功界面
                $('#modalLogin').modal('hide');
                $('#modalLoginSuccess').modal('show');
                //成功后0.9秒跳转到edit.html
                setTimeout(function (){
                    location.href = 'edit.html';
                }, 900);

            } else {
                let msg = result.message;
                //密码错误
                if (result.state === 2) {
                    invalid("log_pass", "密码错误");
                } else {
                    invalid("log_email", msg);
                }
            }
        },
        error: function () {
            toastr.error('通信失败，服务器没有响应');
        }
    });
}


/**
 * 注册功能验证
 */
// 检验注册邮箱是否可用
function checkRegEmail() {
    let email = document.getElementById("reg_email").value;
    if (email.length === 0) return invalid("reg_email", "邮箱不能为空");
    if (!isValidEmail(email)) return invalid("reg_email", "邮箱格式不规范");
    if (isEmailRegistered(email)) return invalid("reg_email", "该用户名已存在");
    return valid("reg_email");
}

// 检验注册昵称是否可用
function checkRegNickname() {
    let nickname = document.getElementById("reg_nick").value;
    if (nickname.length === 0) return invalid("reg_nick", "昵称不能为空");
    if (!isValidNickname(nickname))
        return invalid("reg_nick", "昵称为2-10个连续中文（无空格）或4-20个字母");
    return valid("reg_nick");
}

// 检验注册密码是否可用
function checkRegPassword() {
    let pass = document.getElementById("reg_pass").value;
    if (pass.length === 0) return invalid("reg_pass", "密码不能为空");
    if (!isValidPassword(pass))
        return invalid("reg_pass", "密码为6-20个非空字符");
    return valid("reg_pass");
}

// 检验重复密码是否一致
function checkRegPasswordConfirm() {
    let pass = document.getElementById("reg_pass").value;
    let pass_confirm = document.getElementById("reg_pass_con").value;
    if (pass.length === 0) return invalid("reg_pass", "密码不能为空");
    if (pass_confirm.length === 0) return invalid("reg_pass_con", "请再一次输入密码");
    if (pass !== pass_confirm)
        return invalid("reg_pass_con", "密码不一致，请重新输入");
    return valid("reg_pass_con");
}

// 提交注册表单
function submitRegForm() {
    if (!checkRegEmail() || !checkRegNickname() || !checkRegPassword() || !checkRegPasswordConfirm())
        return false;
    let email = document.getElementById("reg_email").value;
    let nick = document.getElementById("reg_nick").value;
    let pass = document.getElementById("reg_pass").value;
    let pass_con = document.getElementById("reg_pass_con").value;
    let data = {
        "email": email,
        "nickname": nick,
        "password": pass,
        "confirm": pass_con
    };
    $.ajax({
        url: 'user/register.do',
        data: data,
        type: 'post',
        dataType: 'json',
        success: function (result) {
            console.log(result);
            let msg = result.message;
            if (result.state === 0) {
                //注册成功，跳转到登录页面
                $('#btn_log_tab').click();
                toastr.info("验证邮件已发送到您的邮箱中，请您尽快验证");
            } else {
                //密码错误
                if (result.state === 2) invalid("reg_pass", "密码错误");
                else invalid("reg_email", msg);
            }
        },
        error: function () {
            toastr.error('通信失败，服务器没有响应');
        }
    });
}


/**
 * 退出登录
 */
function logout() {
    delCookie('userId');
    delCookie('userEmail');
    location.href = "login.html";
}


/**
 * 修改密码（改版后变成修改信息了）
 */
function changeUserInfo(nick_name, original_password, new_password, final_password, email) {
    //检查新密码的格式和两次密码是否一致

	if (nick_name === '')
		return toastr.error('请填写昵称');
	if (!isValidNickname(nick_name))
		return toastr.error('昵称不可用，应该为2-10个连续中文和字母数字或4-20个英文字母下划线或空格');
	if (original_password === '')
		return toastr.error('请输入原密码');
	if (new_password === '')
		return toastr.error('请输入新密码');
	if (final_password === '')
		return toastr.error('请确认新密码');
	if (!isValidPassword(original_password))
		return toastr.error('原密码错误：密码应为6至20个字符');
	if (!isValidPassword(new_password))
		return toastr.error('新密码错误：密码应为6至20个字符');
	if (new_password !== final_password)
		return toastr.error('新密码不一致，请重新输入');

    $.ajax({
        url: 'user/login.do',
        data: {"email": email, "password": original_password,'nickname': nick_name},
        type: 'post',
        dataType: 'json',
        success: function (result) {
            //返回成功证明原始密码正确
            if (result.state === SUCCESS) {
                //json定义user对象
                $.post(
                    'user/update.do',
                    {
                        name: getCookie('userEmail'),
                        nickName:nick_name,
                        origin: original_password,
                        password: new_password,
                        confirm: final_password
                    },
                    function (result) {
                        if (result.state === SUCCESS) {
                            toastr.success("修改成功,请重新登录");
                            window.location.href = "login.html";
                        } else {
                            toastr.error("修改失败,请稍后重试");
                            window.location.href = "login.html";
                        }
                    }
                )
            } else {
                //如果登录不成功则代表原始密码有误
                toastr.error('原始密码错误');
            }
        },
        error: function (e) {
        	toastr.error('服务器没有响应，请稍后重试');
        }
    });
}
