<%@ page import="de.laser.CustomerTypeService;" %>

<g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.createProperty = function (id, objectTyp,onlyPrivateProperties) {
        var url = '<g:createLink controller="ajaxHtml"  action="createPropertiesModal"/>?id='+id+'&objectTyp='+objectTyp+'&onlyPrivateProperties='+onlyPrivateProperties;
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
                        $(".la-checkAllArea").each(function () {
                              if ($(this).find("input[name='propertyDefinition']").length > 0) {
                                $(this).find(".la-js-checkAll").parent('.segment').css("visibility", "visible");
                              } else {
                                $(this).find(".la-js-checkAll").parent('.segment').css("visibility", "hidden");
                              }
                            });

                            $('.la-js-checkAll').checkbox({
                            // check all children
                                onChecked: function() {
                                    var $childCheckbox  =  $(this).closest('.la-checkAllArea').find('.checkbox');
                                    $childCheckbox.checkbox('check');
                                },
                            // uncheck all children
                                onUnchecked: function() {
                                var $childCheckbox  =  $(this).closest('.la-checkAllArea').find('.checkbox');
                                $childCheckbox.checkbox('uncheck');
                                }
                          });
                    }
                }).modal('show');

                }
        });
    }
    </laser:script>
</g:if>