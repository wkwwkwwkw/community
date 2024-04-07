function login(){
    $("input").removeClass("is-invalid")
    $.post("/login",$('#form').serialize(),function (data) {
        if (data.code === 2000) {
            window.location.href = data.data;
        }else{
            let target=$("#"+data.data);
            target.prev().addClass("is-invalid")
            target.text(data.msg)
        }
    });

}