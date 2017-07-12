var utils = require("./util"),
    bodyParser = require("body-parser"),
    path = require("path"),
    fs = require("fs"),
    Promise = require("promise");
var cheerio = require('cheerio');
var uin;
var key;
var cookie;
var ua2;
var guid;
var auth;
var isRootCAFileExists = require("./certMgr.js").isRootCAFileExists(),
    interceptFlag = false;

//e.g. [ { keyword: 'aaa', local: '/Users/Stella/061739.pdf' } ]
var mapConfig = [],
    configFile = "mapConfig.json";
function saveMapConfig(content, cb) {
    new Promise(function (resolve, reject) {
        var anyproxyHome = utils.getAnyProxyHome(),
            mapCfgPath = path.join(anyproxyHome, configFile);

        if (typeof content == "object") {
            content = JSON.stringify(content);
        }
        resolve({
            path: mapCfgPath,
            content: content
        });
    })
        .then(function (config) {
            return new Promise(function (resolve, reject) {
                fs.writeFile(config.path, config.content, function (e) {
                    if (e) {
                        reject(e);
                    } else {
                        resolve();
                    }
                });
            });
        })
        .catch(function (e) {
            cb && cb(e);
        })
        .done(function () {
            cb && cb();
        });
}
function getMapConfig(cb) {
    var read = Promise.denodeify(fs.readFile);

    new Promise(function (resolve, reject) {
        var anyproxyHome = utils.getAnyProxyHome(),
            mapCfgPath = path.join(anyproxyHome, configFile);

        resolve(mapCfgPath);
    })
        .then(read)
        .then(function (content) {
            return JSON.parse(content);
        })
        .catch(function (e) {
            cb && cb(e);
        })
        .done(function (obj) {
            cb && cb(null, obj);
        });
}
function saveUin(req, res, serverResData, callback) {//将uin、key等信息发送到服务器
    if (key && uin) {
        // callback(serverResData);
        var data = {
            key: encodeURI(key),
            uin: encodeURI(uin),
            cookie: encodeURI(cookie),
            ua2: encodeURI(ua2),
            guid: encodeURI(guid),
            auth: encodeURI(auth),
            url: encodeURI(req.url)
        };
        // key = "";
        // uin = "";
        // ua2 = "";
        // guid = "";
        // auth = "";
        var http = require('http');
        content = require('querystring').stringify(data);
        var options = {
            method: "POST",
            host: "172.30.157.143",//注意没有http://，这是服务器的域名。
            port: 80,
            path: "/weChatSpider/saveUin",//接收程序的路径和文件名
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                "Content-Length": content.length
            }
        };
        var req = http.request(options, function (res) {
            res.setEncoding('utf8');
            res.on('data', function (chunk) {
                console.log(chunk);
                if (!chunk) {
                    console.log(content);
                }
                // callback(serverResData);
            });
        });
        req.on('error', function (e) {
            console.log('problem with request: ' + e.message);
            // callback(serverResData);
        });
        req.write(content);
        req.end();
    } else {
        // callback(serverResData);
    }
}
function rebuildRequestForSpdier(req, newOption) {//将请求头添加uin和key
    if (/mp\/profile_ext\?action=home/i.test(req.url)) {
        if (newOption.headers["x-wechat-key"]) {
            key = newOption.headers["x-wechat-key"];
        } else {
            newOption.headers["x-wechat-key"] = key;
        }
        if (newOption.headers["x-wechat-uin"]) {
            uin = newOption.headers["x-wechat-uin"];
        } else {
            newOption.headers["x-wechat-uin"] = uin;
        }
    }
}
function rebuildRequestForUin(newOption) {//获取请求头内uin、key、cookie等信息
    if (newOption.headers["x-wechat-key"]) {
        key = newOption.headers["x-wechat-key"];
    } else {
        key = "";
    }
    if (newOption.headers["x-wechat-uin"]) {
        uin = newOption.headers["x-wechat-uin"];
    } else {
        uin = "";
    }
    if (newOption.headers["cookie"]) {
        cookie = newOption.headers["cookie"];
    } else {
        cookie = "";
    }
    if (newOption.headers["q-ua2"]) {
        ua2 = newOption.headers["q-ua2"];
    } else {
        ua2 = "";
    }
    if (newOption.headers["q-guid"]) {
        guid = newOption.headers["q-guid"];
    } else {
        guid = "";
    }
    if (newOption.headers["q-auth"]) {
        auth = newOption.headers["q-auth"];
    } else {
        auth = "";
    }
}
function getWeChat(req, res, serverResData, callback) {
    if (/mp\/profile_ext\?action=home/i.test(req.url)) {//当链接地址为公众号历史消息页面时
        try {
            var data;
            if (serverResData.indexOf("此帐号已申请帐号迁移") > -1 || serverResData.indexOf("已停止访问该网页") > -1) {
                data = {
                    data: encodeURIComponent("faild"),
                    uin: encodeURIComponent(uin),
                    url: encodeURIComponent(req.url)
                }
            } else {
                var reg = /var msgList = \'(.*?)\';/;//定义历史消息正则匹配规则
                var ret = reg.exec(serverResData.toString());//转换变量为string
                var replace = ["&#39;", "'", "&quot;", '"', "&nbsp;", " ", "&gt;", ">", "&lt;", "<", "&amp;", "&", "&yen;", "¥", "\\\\/", "/"];
                var dataStr = ret[1];
                for (var i = 0, str = this; i < replace.length; i += 2) {
                    dataStr = dataStr.replace(new RegExp(replace[i], 'g'), replace[i + 1]);
                }
                //console.log(dataStr);
                var dataList = JSON.parse(dataStr).list;//将列表内容解析为JSON 并提取需要的信息进行二次封装
                var arlist = [];
                for (var data in dataList) {
                    var date = dataList[data].comm_msg_info.datetime + '';
                    if (dataList[data].app_msg_ext_info != undefined) {
                        for (var j in dataList[data].app_msg_ext_info.multi_app_msg_item_list) {
                            var map = {};
                            map.title = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].title;
                            map.url = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].content_url;
                            map.datetime = date;
                            map.cover = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].cover;
                            arlist.push(map);
                        }
                        var map = {};
                        map.title = dataList[data].app_msg_ext_info.title;//文章标题
                        map.url = dataList[data].app_msg_ext_info.content_url;//文章url
                        map.cover = dataList[data].app_msg_ext_info.cover;//文章封面地址
                        map.datetime = date;//文章发布时间（秒）
                        arlist.push(map);
                    }
                }
                data = {
                    data: encodeURI(JSON.stringify(arlist)),//封装后的文章列表
                    uin: encodeURIComponent(uin),
                    url: encodeURI(req.url)//历史页的URL 用于提取biz
                };
            }
            var http = require('http');
            content = require('querystring').stringify(data);//将文章列表发送到服务端
            var options = {
                method: "POST",
                host: "172.30.157.143",//注意没有http://，这是服务器的域名。
                port: 80,
                path: "/weChatSpider/getNextWeChat",//接收程序的路径和文件名
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    "Content-Length": content.length
                }
            };
            var req = http.request(options, function (res) {
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    console.log(chunk);
                    // nextUrl = chunk;
                    // if(chunk != null){
                    //     chunk = "<script>setTimeout(function(){window.location.href='https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=" + chunk + "&scene=124#wechat_redirect';},120000);</script>";
                    // }
                    callback(chunk + serverResData);//在腾讯服务器返回值前加入定时跳转
                });
            });
            req.on('error', function (e) {
                console.log('problem with request: ' + e.message);
            });
            req.write(content);
            req.end();
        } catch (e) {
            console.log(e)
            callback(serverResData);
        }
    } else {
        callback(serverResData);
    }
}
setTimeout(function () {
    //load saved config file
    getMapConfig(function (err, result) {
        if (result) {
            mapConfig = result;
        }
    });
}, 1000);


function getSougouWeChat(req, res, serverResData, callback) {//通过搜狗接口抓取，由于ip限制已弃用
    if (req.url.indexOf("weixin.sogou.com/weixinwap?query=") > -1) {//当链接地址为公众号历史消息页面时(第二种页面形式)
        console.log("in:" + req.url);
        try {
            var account = req.url.substring(req.url.indexOf("query=") + 6, req.url.indexOf("&type"));
            console.log(account);
            var $ = cheerio.load(serverResData);
            var elements = $("div.gzh-box");
            elements.each(function (item) {
                var cap = $(this);
                var name = cap.find("p.gzh-name").text().substring(4).toLowerCase();
                var url = cap.find("a").attr("href");
                console.log(account + " === " + name);
                if (name === account) {
                    serverResData = "<script>setTimeout(function(){window.location.href='" + url + "';},1000);</script>" + serverResData;
                }
            });
            callback(serverResData);
            // console.log(elements);
            // for (var element in elements) {
            //     var name = elements[element].find("p[gzh-name]").text().substring(4);
            //     console.log(name);
            //     var url = elements[element].find("a").attr("href");
            //     console.log(url)
            // }
        } catch (e) {
            console.log(e);
        }
    } else if (req.url.indexOf("mp.weixin.qq.com/profile?src=3") > -1) {
        try {
            console.log("else if in");
            var data;
            if (serverResData.indexOf("此帐号已申请帐号迁移") > -1 || serverResData.indexOf("已停止访问该网页") > -1 || serverResData.indexOf("此帐号已自主注销，内容无法查看") > -1) {
                data = {
                    data: encodeURIComponent("faild"),
                    account: encodeURIComponent("")
                }
            } else if (serverResData.indexOf("请输入验证码") > -1) {
                data = {
                    data: encodeURIComponent("verify"),
                    account: encodeURIComponent("")
                }
            } else {
                var accountReg = /var name=\"(.*?)"\|/;
                var account = accountReg.exec(serverResData.toString())[1];
                var reg = /var msgList = (.*?)}}]};/;//定义历史消息正则匹配规则（和第一种页面形式的正则不同）
                var ret = reg.exec(serverResData.toString());//转换变量为string
                var replace = ["&#39;", "\'", "&quot;", '\"', "&nbsp;", " ", "&gt;", ">", "&lt;", "<", "&amp;", "&", "&yen;", "¥", "\\\\/", "/"];
                var dataStr = ret[1] + "}}]}";
                // for (var i = 0; i < replace.length; i += 2) {
                //     dataStr = dataStr.replace(new RegExp(replace[i], 'g'), replace[i + 1]);
                // }
                // console.log(dataStr);
                var dataList = JSON.parse(dataStr).list;
                var arlist = [];
                for (var data in dataList) {
                    var date = dataList[data].comm_msg_info.datetime + '';
                    if (dataList[data].app_msg_ext_info != undefined) {
                        for (var j in dataList[data].app_msg_ext_info.multi_app_msg_item_list) {
                            var map = {};
                            map.title = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].title;
                            map.url = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].content_url;
                            map.datetime = date;
                            map.cover = dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].cover;
                            arlist.push(map);
                        }
                        var map = {};
                        map.title = dataList[data].app_msg_ext_info.title;
                        map.url = dataList[data].app_msg_ext_info.content_url;
                        map.cover = dataList[data].app_msg_ext_info.cover;
                        map.datetime = date;
                        arlist.push(map);
                    }
                }
                data = {
                    data: encodeURI(JSON.stringify(arlist)),
                    account: encodeURIComponent(account),
                };
            }
            var http = require('http');
            content = require('querystring').stringify(data);
            var options = {
                method: "POST",
                host: "172.30.157.86",//注意没有http://，这是服务器的域名。
                port: 80,
                path: "/weChatSpider/getNextSougou",//接收程序的路径和文件名
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    "Content-Length": content.length
                }
            };
            var req = http.request(options, function (res) {
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    console.log("receive successful:" + chunk);
                    if (chunk !== "null") {
                        serverResData = "<script>setTimeout(function(){window.location.href='http://weixin.sogou.com/weixinwap?query=" + chunk + "&type=1&ie=utf8&_sug_=y&_sug_type_=&s_from=input';},10000);</script>" + serverResData;
                    }
                    callback(serverResData);
                });
            });
            req.on('error', function (e) {
                console.log('problem with request: ' + e.message);
            });
            req.write(content);
            req.end();
        } catch (e) {
            callback(serverResData);
            console.log(e)
        }
    } else {
        callback(serverResData);
    }
}
function get360(req, res, serverResData, callback) {//抓取360和爱奇艺头条文章
    if (req.url.indexOf("sdk.look.360.cn/sdkv2/list") > -1 || req.url.indexOf("v.sj.360.cn/video/list") > -1 ) {
        try {
            console.log("if in");
            var data;
            var dataList = JSON.parse(serverResData).data.res;
            var arlist = [];
            for (var data in dataList) {
                var map = {};
                map.title = dataList[data].t;
                map.pageUrl = dataList[data].u;
                map.preUrl = dataList[data].rawurl;
                map.cateName = dataList[data].f;
                map.site = "360";
                arlist.push(map);
            }
            data = {
                otherArticles: JSON.stringify(arlist),
                originalStr: serverResData.toString("utf8")
            };
            var http = require('http');
            var content = require('querystring').stringify(data);
            var options = {
                method: "POST",
                host: "172.30.157.143",//注意没有http://，这是服务器的域名。
                port: 80,
                path: "/weChatSpider/getOtherArticle",//接收程序的路径和文件名
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    "Content-Length": content.length
                }
            };
            var req = http.request(options, function (res) {
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    console.log("receive successful:" + chunk);
                });
            });
            req.on('error', function (e) {
                console.log('problem with request: ' + e.message);
            });
            req.write(content);
            req.end();
        } catch (e) {
            console.log(e)
        }
    } else if (req.url.indexOf("toutiao.iqiyi.com/api/news/v2/feeds") > -1) {
        try {
            console.log("else if in");
            var data;
            var dataList = JSON.parse(serverResData).data.feeds;
            var arlist = [];
            for (var data in dataList) {
                if (dataList[data].subFeeds) {
                    var sub = dataList[data].subFeeds;
                    for (var tem in sub) {
                        var map = {};
                        map.title = sub[tem].base.displayName !== null ? sub[tem].base.displayName : "";
                        map.pageUrl = sub[tem].h5PageUrl !== null ? sub[tem].h5PageUrl : "";
                        map.preUrl = sub[tem].original.contentUrl !== null ? sub[tem].original.contentUrl : "";
                        map.cateName = sub[tem].weMedia.nickName !== null ? sub[tem].weMedia.nickName : "";
                        map.site = "iqiyi";
                        arlist.push(map);
                    }
                } else {
                    var map = {};
                    map.title = dataList[data].base.displayName !== null ? dataList[data].base.displayName : "";
                    map.pageUrl = dataList[data].h5PageUrl !== null ? dataList[data].h5PageUrl : "";
                    map.preUrl = dataList[data].original.contentUrl !== null ? dataList[data].original.contentUrl : "";
                    map.cateName = dataList[data].weMedia.nickName !== null ? dataList[data].weMedia.nickName : "";
                    map.site = "iqiyi";
                    arlist.push(map);
                }
            }
            data = {
                otherArticles: JSON.stringify(arlist),
                originalStr: serverResData.toString("utf8")
            };
            var http = require('http');
            var content = require('querystring').stringify(data);
            var options = {
                method: "POST",
                host: "172.30.157.143",//注意没有http://，这是服务器的域名。
                port: 80,
                path: "/weChatSpider/getOtherArticle",//接收程序的路径和文件名
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    "Content-Length": content.length
                }
            };
            var req = http.request(options, function (res) {
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    console.log("receive successful:" + chunk);
                });
            });
            req.on('error', function (e) {
                console.log('problem with request: ' + e.message);
            });
            req.write(content);
            req.end();
        } catch (e) {
            console.log(e)
        }
    } else if (req.url.indexOf("toutiao.iqiyi.com/api/news/v1") > -1) {
        try {
            console.log("else if in");
            var data;
            var dataList = JSON.parse(serverResData).data.new;
            var arlist = [];
            for (var data in dataList) {
                if (dataList[data].subFeeds) {
                    var sub = dataList[data].subFeeds;
                    for (var tem in sub) {
                        var map = {};
                        map.title = sub[tem].base.displayName !== null ? sub[tem].base.displayName : "";
                        map.pageUrl = sub[tem].h5PageUrl !== null ? sub[tem].h5PageUrl : "";
                        map.preUrl = sub[tem].original.contentUrl !== null ? sub[tem].original.contentUrl : "";
                        map.cateName = sub[tem].weMedia.nickName !== null ? sub[tem].weMedia.nickName : "";
                        map.site = "iqiyi";
                        arlist.push(map);
                    }
                } else {
                    var map = {};
                    map.title = dataList[data].base.displayName !== null ? dataList[data].base.displayName : "";
                    map.pageUrl = dataList[data].h5PageUrl !== null ? dataList[data].h5PageUrl : "";
                    map.preUrl = dataList[data].original.contentUrl !== null ? dataList[data].original.contentUrl : "";
                    map.cateName = dataList[data].weMedia.nickName !== null ? dataList[data].weMedia.nickName : "";
                    map.site = "iqiyi";
                    arlist.push(map);
                }
            }
            data = {
                otherArticles: JSON.stringify(arlist),
                originalStr: serverResData.toString("utf8")
            };
            var http = require('http');
            var content = require('querystring').stringify(data);
            var options = {
                method: "POST",
                host: "172.30.157.143",//注意没有http://，这是服务器的域名。
                port: 80,
                path: "/weChatSpider/getOtherArticle",//接收程序的路径和文件名
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    "Content-Length": content.length
                }
            };
            var req = http.request(options, function (res) {
                res.setEncoding('utf8');
                res.on('data', function (chunk) {
                    console.log("receive successful:" + chunk);
                });
            });
            req.on('error', function (e) {
                console.log('problem with request: ' + e.message);
            });
            req.write(content);
            req.end();
        } catch (e) {
            console.log(e)
        }
    }
    callback(serverResData);
}
module.exports = {
    token: Date.now(),
    summary: function () {
        var tip = "the default rule for AnyProxy.";
        if (!isRootCAFileExists) {
            tip += "\nRoot CA does not exist, will not intercept any https requests.";
        }
        return tip;
    },

    shouldUseLocalResponse: function (req, reqBody) {
        //intercept all options request
        var simpleUrl = (req.headers.host || "") + (req.url || "");
        mapConfig.map(function (item) {
            var key = item.keyword;
            if (simpleUrl.indexOf(key) >= 0) {
                req.anyproxy_map_local = item.local;
                return false;
            }
        });


        return !!req.anyproxy_map_local;
    },

    dealLocalResponse: function (req, reqBody, callback) {
        if (req.anyproxy_map_local) {
            fs.readFile(req.anyproxy_map_local, function (err, buffer) {
                if (err) {
                    callback(200, {}, "[AnyProxy failed to load local file] " + err);
                } else {
                    var header = {
                        'Content-Type': utils.contentType(req.anyproxy_map_local)
                    };
                    callback(200, header, buffer);
                }
            });
        }
    },

    replaceRequestProtocol: function (req, protocol) {
    },

    replaceRequestOption: function (req, option) {
        var newOption = option;
        // if (req.url.indexOf("pass_ticket") === -1){
        //     newOption.host = "www.baidu.com";
        //     newOption.port = "80";
        //     req.url.replace("profile_ext","null");
        // }
        // rebuildRequestForUin(newOption);
        // rebuildRequestForSpdier(req,newOption);
        return newOption;
    },

    replaceRequestData: function (req, data) {
    },

    replaceResponseStatusCode: function (req, res, statusCode) {
    },

    replaceResponseHeader: function (req, res, header) {
    },

    // Deprecated
    // replaceServerResData: function(req,res,serverResData){
    //     return serverResData;
    // },

    replaceServerResDataAsync: function (req, res, serverResData, callback) {
        //if(/mp\/getmasssendmsg/i.test(req.url)){
        try {
            // saveUin(req, res, serverResData, callback);
            getWeChat(req, res, serverResData, callback);
            // getSougouWeChat(req, res, serverResData, callback);
            // get360(req, res, serverResData, callback);
        } catch (e) {
            console.log(e)
            callback(serverResData);
        }
    },

    pauseBeforeSendingResponse: function (req, res) {
    },

    shouldInterceptHttpsReq: function (req) {
        return interceptFlag;
    },

    //[beta]
    //fetch entire traffic data
    fetchTrafficData: function (id, info) {
    },

    setInterceptFlag: function (flag) {
        interceptFlag = flag && isRootCAFileExists;
    },

    _plugIntoWebinterface: function (app, cb) {

        app.get("/filetree", function (req, res) {
            try {
                var root = req.query.root || utils.getUserHome() || "/";
                utils.filewalker(root, function (err, info) {
                    res.json(info);
                });
            } catch (e) {
                res.end(e);
            }
        });

        app.use(bodyParser.json());
        app.get("/getMapConfig", function (req, res) {
            res.json(mapConfig);
        });
        app.post("/setMapConfig", function (req, res) {
            mapConfig = req.body;
            res.json(mapConfig);

            saveMapConfig(mapConfig);
        });

        cb();
    },

    _getCustomMenu: function () {
        return [
            // {
            //     name:"test",
            //     icon:"uk-icon-lemon-o",
            //     url :"http://anyproxy.io"
            // }
        ];
    }

};