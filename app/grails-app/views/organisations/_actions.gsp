<%@page import="de.laser.helper.RDStore" %>
<g:if test="${editable}">

    <g:if test="${actionName == 'list'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="organisations" action="create" message="org.create_new.label"/>
        </semui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'listInstitution'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="organisations" action="findInstitutionMatches" message="org.create_new_Institution.label"/>
        </semui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'listProvider'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="organisations" action="findProviderMatches" message="org.create_new_Provider.label"/>
        </semui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'show'}">
        <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
            <semui:actionsDropdown>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalgruppen konfigurieren" />
            </semui:actionsDropdown>
        </g:if>
    </g:if>
    <g:if test="${actionName == 'documents'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="template.documents.add" controller="organisations" action="editDocument" params="[org:org.id]"/>
        </semui:actionsDropdown>
    </g:if>
</g:if>
<r:script>
    var isClicked = false;
    $('[href="#modalCreateDocument"]').on('click', function(e) {
        e.preventDefault();

        if(!isClicked) {
            isClicked = true;
            $.ajax({
                url: "<g:createLink controller="${controllerName}" action="editDocument" params="[org:orgInstance?.id]"/>" //null check needed because orgInstance is not set from everywhere
            }).done( function(data) {
                $('.ui.dimmer.modals > #modalCreateDocument').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                    onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#modalCreateDocument');
                        r2d2.initDynamicXEditableStuff('#modalCreateDocument');
                        toggleTarget();
                        showHideTargetableRefdata();
                    },
                    detachable: true,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            });
            setTimeout(function(){
                isClicked = false;
            },800);
        }

    });

    function showHideTargetableRefdata() {
        console.log($(org).val());
        if($(org).val().length === 0) {
            $("[data-value='com.k_int.kbplus.RefdataValue:${RDStore.SHARE_CONF_UPLOADER_AND_TARGET.id}']").hide();
        }
        else {
            $("[data-value='com.k_int.kbplus.RefdataValue:${RDStore.SHARE_CONF_UPLOADER_AND_TARGET.id}']").show();
        }
    }

    function toggleTarget() {
        if($("#hasTarget")[0].checked)
            $("#target").show();
        else
            $("#target").hide();
    }
</r:script>
