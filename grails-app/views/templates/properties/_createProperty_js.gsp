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
                        $(".propDefFilter").on('input', function() {
                            let table = $(this).attr('data-forTable');
                            $("#"+table+" td.pdName:containsInsensitive_laser('"+$(this).val()+"')").parent("tr").show();
                            $("#"+table+" td.pdName:not(:containsInsensitive_laser('"+$(this).val()+"'))").parent("tr").hide();
                        });
                    }
                }).modal('show');

                }
        });

        //own selector for case-insensitive :contains
        jQuery.expr[':'].containsInsensitive_laser = function(a, i, m) {
            return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
        };
    }
    </laser:script>
</g:if>