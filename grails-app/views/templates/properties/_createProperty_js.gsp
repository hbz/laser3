<g:if test="${editable}">
    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.createProperty = function (id, objectTyp) {
        var url = '<g:createLink controller="ajaxHtml" action="createPropertiesModal"/>?id='+id+'&objectTyp='+objectTyp;
        $.ajax({
            url: url,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#createPropertyModal").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                        r2d2.initDynamicUiStuff('#createPropertyModal');
                        r2d2.initDynamicXEditableStuff('#createPropertyModal');
                    }
                }).modal('show');
                }
        });
    }
    </laser:script>
</g:if>