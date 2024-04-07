$(function () {
    $("#sendBtn").on("click",publish($("#sendModal"),"/message/send",{"toId": $("#recipient-id").val(), "content": $("#message-text").val()}));
    $(".close").click(delete_msg);
});
function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}