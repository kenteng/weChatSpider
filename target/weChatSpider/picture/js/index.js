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
            url: "./startSpider",
            success: function (result) {
                alert(result);
            }
        });
    });
    $("#stopSpider").click(function () {
        $.ajax({
            type: "POST",
            url: "./stopSpider",
            success: function (result) {
                alert(result);
            }
        });
    });
    $("#startDownload").click(function () {
        $.ajax({
            type: "POST",
            url: "./startDownload",
            success: function (result) {
                alert(result);
            }
        });
    });
    $("#stopDownload").click(function () {
        $.ajax({
            type: "POST",
            url: "./stopDownload",
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
        url: "./getStatus",
        success: function (result) {
            $("#table").html(buildTable(result));
            $("#preId").click(function () {
                var preId = $(this).parents("tr").find("td input[name='preId']").val();
                $.ajax({
                    type: "POST",
                    url: "./setStartId",
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
                var proxyPort = $(this).parents("tbody").find("td input[name='proxyPort']").val();
                $.ajax({
                    type: "POST",
                    url: "./setProxy",
                    data: {
                        proxyIp: proxyIp,
                        proxyPort: proxyPort
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#downloadedPicNum").click(function () {
                var downloadedPicNum = $(this).parents("tr").find("td input[name='downloadedPicNum']").val();
                $.ajax({
                    type: "POST",
                    url: "./setDownloadNum",
                    data: {
                        downloadedPicNum: downloadedPicNum
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#sleepTime").click(function () {
                var sleepTime = $(this).parents("tr").find("td input[name='sleepTime']").val();
                $.ajax({
                    type: "POST",
                    url: "./setSleepTime",
                    data: {
                        sleepTime: sleepTime
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#downloadSleepTime").click(function () {
                var downloadSleepTime = $(this).parents("tr").find("td input[name='downloadSleepTime']").val();
                $.ajax({
                    type: "POST",
                    url: "./setDownloadSleepTime",
                    data: {
                        downloadSleepTime: downloadSleepTime
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#spiderThreadNum").click(function () {
                var spiderThreadNum = $(this).parents("tr").find("td input[name='spiderThreadNum']").val();
                $.ajax({
                    type: "POST",
                    url: "./setSpiderThreadNum",
                    data: {
                        spiderThreadNum: spiderThreadNum
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#downloadThreadNum").click(function () {
                var downloadThreadNum = $(this).parents("tr").find("td input[name='downloadThreadNum']").val();
                $.ajax({
                    type: "POST",
                    url: "./setDownloadThreadNum",
                    data: {
                        downloadThreadNum: downloadThreadNum
                    },
                    success: function (result) {
                        alert(result);
                    }
                });
            });
            $("#cookie").click(function () {
                var cookie = $(this).parents("tr").find("td textarea[name='cookie']").val();
                $.ajax({
                    type: "POST",
                    url: "./setCoookie",
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
    htmlStr += "<tr><th style='text-align: center;' >抓取运行状态</th><td style='text-align: center';><input class='input' type='text' name='spiderState' value='" + res.spiderState + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >下载运行状态</th><td style='text-align: center';><input class='input' type='text' name='downloadState' value='" + res.downloadState + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >抓取队列大小</th><td style='text-align: center';><input class='input' type='text' name='spiderStackNum' value='" + res.spiderStackNum + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >下载队列大小</th><td style='text-align: center';><input class='input' type='text' name='downloadStackNum' value='" + res.downloadStackNum + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >已抓取底页数量</th><td style='text-align: center';><input class='input' type='text' name='totalCount' value='" + res.totalCount + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >当前正在抓取ID</th><td style='text-align: center';><input class='input' type='text' name='currentId' value='" + res.currentId + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >当前正在下载ID</th><td style='text-align: center';><input class='input' type='text' name='currentDownloadId' value='" + res.currentDownloadId + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >待下载任务总量</th><td style='text-align: center';><input class='input' type='text' name='total' value='" + res.total + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >已下载任务总量</th><td style='text-align: center';><input class='input' type='text' name='downloaded' value='" + res.downloaded + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >下载出错任务总量</th><td style='text-align: center';><input class='input' type='text' name='error' value='" + res.error + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >已下载图片总量</th><td style='text-align: center';><input class='input' type='text' name='downloadedPicNum' value='" + res.downloadedPicNum + "'</td><td <div><button id='downloadedPicNum'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >当前抓取生产起始ID</th><td style='text-align: center';><input class='input' type='text' name='preId' value='" + res.preId + "'</td><td <div><button id='preId'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >抓取线程sleep时间</th><td style='text-align: center';><input class='input' type='text' name='sleepTime' value='" + res.sleepTime + "'</td><td <div><button id='sleepTime'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >下载线程sleep时间</th><td style='text-align: center';><input class='input' type='text' name='downloadSleepTime' value='" + res.downloadSleepTime + "'</td><td <div><button id='downloadSleepTime'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >抓取线程数</th><td style='text-align: center';><input class='input' type='text' name='spiderThreadNum' value='" + res.spiderThreadNum + "'</td><td <div><button id='spiderThreadNum'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >下载线程数</th><td style='text-align: center';><input class='input' type='text' name='downloadThreadNum' value='" + res.downloadThreadNum + "'</td><td <div><button id='downloadThreadNum'>修改</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >代理状态</th><td style='text-align: center';><input class='input' type='text' name='isProxyDown' value='" + res.isProxyDown + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >Cookie状态</th><td style='text-align: center';><input class='input' type='text' name='isCookieDown' value='" + res.isCookieDown + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >代理Ip</th><td style='text-align: center';><input class='input' type='text' name='proxyIp' value='" + res.proxyIp + "'</td><td <div><button id='proxyIp'>修改代理</button></div></td></tr>" +
        "<tr><th style='text-align: center;' >代理Ip端口</th><td style='text-align: center';><input class='input' type='text' name='proxyPort' value='" + res.proxyPort + "'</td><td <div></div></td></tr>" +
        "<tr><th style='text-align: center;' >cookie</th><td style='text-align: center';><textarea class='input' type='text' name='cookie' style='margin: 0px; width: 400px; height: 50px;'>" + res.cookie + "</textarea></td><td <div><button id='cookie'>修改</button></div></td></tr></tbody></table>";
    return htmlStr;
}
