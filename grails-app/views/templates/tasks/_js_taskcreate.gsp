<asset:script type="text/javascript">
    function taskcreate() {

        $.ajax({
            url: '<g:createLink controller="myInstitution" action="modal_create"/>',
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalCreateTask").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                            ajaxPostFunc();
                    }
                }).modal('show');
            }
        });
    }
</asset:script>