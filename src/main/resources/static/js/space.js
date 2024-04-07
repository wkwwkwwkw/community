$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    let btn = $(this);
    $.post(
        "/user/follow",
        {"entityType": 0, "entityId": parseInt(btn.prev().val()), "authorId": parseInt(btn.prev().val())},
        function (data) {
            if (data.code === 2000) {
                let number=$("#follower-num")
                if (data.data) {
                    btn.text("已关注").removeClass("btn-info").addClass("btn-secondary");
                    number.text(parseInt(number.text()) + 1);
                } else {
                    btn.text("关注TA").removeClass("btn-secondary").addClass("btn-info");
                    number.text(parseInt(number.text()) - 1);
                }
            } else {
                alert(data.msg);
            }
        }
    );
}