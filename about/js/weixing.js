var weixin = {
    config: {
        url:'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx2d2e96dd0762eada&redirect_uri='+encodeURIComponent(window.location.href)+'&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect',
        userInfo:JSON.parse(localStorage.getItem('MY_USER_INFO')),
        api:'http://192.168.1.114:8080/zyt-http/'
    },
    isweixin: function() {
        var ua = window.navigator.userAgent.toLowerCase();
        if(ua.match(/MicroMessenger/i) == 'micromessenger'){
            return true;
        } else {
            return false;
        }
    },
    getQueryString: function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)","i");
        var r = window.location.search.substr(1).match(reg);
        if (r!=null) return unescape(r[2]); return null;
    },
    getUser : function(code) {
        $.ajax({
            type: 'get',
            url: weixin.config.api + '/third/weixin/xxx?code='+code,
            cache:false,
            async: false,
            dataType: 'jsonp',
            jsonp: 'jsonpcallback',
            success: function(json){
                localStorage.setItem('MY_USER_INFO',JSON.stringify(json));
            },
            error: function(json) {
                console.log(json);
            }
        })
    },
    getUserInfo:function(){
        if(weixin.config.userInfo != null){
            return JSON.parse(localStorage.getItem('MY_USER_INFO'));
        }else{
            if(weixin.getQueryString('code') != null){
                weixin.getUser(weixin.getQueryString('code'));
                return JSON.parse(localStorage.getItem('MY_USER_INFO'));
            }else{
                window.location.href = weixin.config.url;
            }
        }
    }
}