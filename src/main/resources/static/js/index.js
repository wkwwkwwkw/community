function publish(target,path,form) {
    $("#"+target).modal("hide");
    $.post(path, $('#'+form).serialize(), function (res) {
            $("#hintBody").text(res.msg);
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                if (res.code === 2000) {
                    window.location.reload();
                }
            }, 2000);
        }
    );
    return true;
}