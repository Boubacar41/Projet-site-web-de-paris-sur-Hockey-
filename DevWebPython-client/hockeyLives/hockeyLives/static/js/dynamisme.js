<script type="text/javascript">

function resizeBackground()
{
    $("#background").css ({
        width: $(window).width,
        height: $(window).height
    })
}
resizeBackground();

$(window).resize(function(){
resizeBackground()
})

</script>