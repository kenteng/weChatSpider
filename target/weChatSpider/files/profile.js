var c = context.get("d");

c = c.replace('document.', '').replace('seajs.use', '');

var f = '';
var replace = ["&amp;", "&"];
eval(c + '');
var dataList = msgList.list;
var arlist = context.get("arlist");
for (var data in dataList) {
    var date = dataList[data].comm_msg_info.datetime + '';
    if (dataList[data].app_msg_ext_info != undefined) {
        for (var j in dataList[data].app_msg_ext_info.multi_app_msg_item_list) {
            var map = new java.util.HashMap();
            map.put("title", dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].title);
            map.put("url", dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].content_url);
            map.put("datetime", date);
            map.put("cover", dataList[data].app_msg_ext_info.multi_app_msg_item_list[j].cover);
            arlist.add(map);
        }

        var map = new java.util.HashMap();

        map.put("title", dataList[data].app_msg_ext_info.title);
        map.put("url", dataList[data].app_msg_ext_info.content_url);
        map.put("cover", dataList[data].app_msg_ext_info.cover);
        map.put("datetime", date);
        arlist.add(map);
    }
}

