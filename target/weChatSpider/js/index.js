/**
 * Created by zhusy on 2016/9/13.
 */
$(document).ready(function () {
    $("#freshStatus").click(function () {
        fresh();
    });
    $("#startSpider").click(function () {
        $.ajax({
            type: "POST",
            url: "./picture/startSpider",
            success: function (result) {
                alert(result);
            }
        });
    });
    $("#stopSpider").click(function () {
        $.ajax({
            type: "POST",
            url: "./picture/stopSpider",
            success: function (result) {
                alert(result);
            }
        });
    });
   fresh();
});
function fresh() {
    $.ajax({
        type: "POST",
        url: "./picture/getStatus",
        success: function (result) {
            $("#table").html(buildTable(result));
            $("#preId").click(function () {
                var preId = $(this).parents("tr").find("td input[name='preId']").val();
                $.ajax({
                    type: "POST",
                    url: "./picture/setStartId",
                    data: {
                        startId: preId
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#proxyIp").click(function () {
                var proxyIp = $(this).parents("tr").find("td input[name='proxyIp']").val();
                var proxyPort = $(this).parents("tr").find("td input[name='proxyPort']").val();
                $.ajax({
                    type: "POST",
                    url: "./picture/setStartId",
                    data: {
                        proxyIp: proxyIp,
                        proxyPort: proxyPort
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#cookie").click(function () {
                var cookie = $(this).parents("tr").find("td input[name='cookie']").val();
                $.ajax({
                    type: "POST",
                    url: "./picture/setCoookie",
                    data: {
                        cookie: cookie
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
        }
    });
}
function buildTable(res) {
    if (!res || res.lenght <= 0) {
        return "<p style='line-height :5em;text-align: center;font-size: 14px;'>无数据！</p>";
    }
    var htmlStr = " <table class='table' id = 'tablebody'><thead><tr><th style='text-align: center;'>参数名</th><th style='text-align: center;'>参数值</th><th style='text-align: center;'>操作</th></tr></thead><tbody>";
    htmlStr += "<td><th style='text-align: center;' >总抓取底页数量</th><td style='text-align: center';><input class='input' type='text' name='totalCount' value='/>" + res.totalCount + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >当前正在抓取ID</th><td style='text-align: center';><input class='input' type='text' name='currentId' value='/>" + res.currentId + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >当前抓取生产起始ID</th><td style='text-align: center';><input class='input' type='text' name='preId' value='/>" + res.preId + "'</td><td <div><button id='preId'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >代理Ip</th><td style='text-align: center';><input class='input' type='text' name='proxyIp' value='/>" + res.proxyIp + "'</td><td <div><button id='proxyIp'>修改代理</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >代理Ip端口</th><td style='text-align: center';><input class='input' type='text' name='proxyPort' value='/>" + res.proxyPort + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >cookie</th><td style='text-align: center';><input class='input' type='text' name='cookie' value='/>" + res.cookie + "'</td><td <div><button id='cookie'>修改</button></div></td></tr></tbody></table>";
    return htmlStr;
}
