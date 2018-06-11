
// function autoPlayAudio() {
//     wx.config({
//         // 配置信息, 即使不正确也能使用 wx.ready
//         debug: false,
//         appId: 'wx032c564a32073fee',
//         timestamp: 1,
//         nonceStr: '',
//         signature: '',
//         jsApiList: []
//     });
//     wx.ready(function() {
//         var globalAudio=document.getElementById("musicAudio");
//         globalAudio.play();
//     });
// };

// 解决ios音乐不自动播放的问题
// autoPlayAudio();
// <!--截取 ? & 之间 namebutt= 后面的值一般用来截取url?后面的参数-->

function getUrlKey(name){
    return decodeURIComponent((new RegExp('[?|&]'+name+'='+'([^&;]+?)(&|#|;|$)').exec(location.href)||[,""])[1].replace(/\+/g,'%20'))||null;
}
//设置 获得  删除Cookie
Vue.prototype.setCookie = function(c_name, value, expiredays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + expiredays);
    document.cookie = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
}
function getCookie(name) {
    var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
    if (arr = document.cookie.match(reg)){
        return (arr[2]);
    }else{
        return null;
    }
}
Vue.prototype.getCookie = getCookie;
Vue.prototype.delCookie = function(name){
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval = getCookie(name);
    if (cval != null)
        document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
}
<!-- 测试 -->
var root = "http://192.168.1.114:8080/zyt-http/";

<!--正式-->
// var root = "http://api.54zyt.com:81/zyt-http/";
Vue.prototype.$api= {
    post: function(url, params, success, failure) {
        return fetch(root+url,{method:'POST',
            body:JSON.stringify(params),
            headers: {
                "Content-Type": "application/json"
            }

        }).then(function(res){
            return res.json();

        }).then(function(json){
            //登录的时候实名认证情况单独处理接口
            if(url == 'worker/user/login_web'){

                success(json);
            }else{
                if(json.status==0){
                    success(json);
                }else{
                    failure(json);
                }
            }


        }).catch(function(err){
            failure(err);
        })
    }

}
