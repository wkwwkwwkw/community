function like(btn, entityType, entityId, userId) {
    $.post("/like", {"entityType": entityType, "entityId": entityId, "userId": userId}, function (data) {
            if (data.code === 2000) {
                data=data.data
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus ? "已点赞" : "赞");
            } else {
                alert(data.msg);
            }
        },"json");
}
function operation(method,id,status){
    $.post("/article/"+method+"/"+id,{"status":status}, function (data) {
        if (data.code === 2000) {
            if(method === "delete"){
                location.href="/";
                return;
            }
            window.location.reload();
        } else {
            alert(data.msg);
        }
    },"json");
}
function reply(btn){}