<g:if test="${showOpener}">

    <button class="ui button" id="audit_config_opener" data-oid="${ownobj.class.name}:${ownobj.id}">AUDIT_CONFIG</button>

    <r:script>
        $('#audit_config_opener').on( 'click', function() {
            $.ajax({
                url: '<g:createLink controller="ajax" action="showAuditConfigManager"/>?target='+ $(this).attr('data-oid'),
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#audit_config_modal").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({
                        onVisible: function() {
                            $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                            //ajaxPostFunc();
                        }
                    }).modal('show')
                }
            });
        });
    </r:script>

</g:if>
