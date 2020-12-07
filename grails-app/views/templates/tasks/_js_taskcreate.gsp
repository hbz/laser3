<laser:script>
    JSPC.taskcreate = function () {

        $.ajax({
            url: '<g:createLink controller="myInstitution" action="modal_create"/>',
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalCreateTask").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                            JSPC.callbacks.dynPostFunc();
                    }
                }).modal('show');
            }
        });
    }
</laser:script>