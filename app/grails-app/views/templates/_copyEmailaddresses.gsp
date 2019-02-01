<!-- _copyEmailAddresses.gsp -->
<%@ page import="groovy.json.JsonSlurperClassic; com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail"               value="${RDStore.CCT_EMAIL}"/>
    <g:set var="rdvGeneralContactPrs"   value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS}"/>
    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues('Person Function')}"/>

    <div class="field">
        <label>Funktion</label>
        <laser:select class="ui dropdown search"
                      name="roleTypeMultiSelect"
                      multiple=""
                      from="${rdvAllPersonFunctions}"
                      optionKey="id"
                      optionValue="value"
                      value="${rdvGeneralContactPrs.id}"/>
    </div>
    <br><br>
    <g:set var="generalContactPrsMap" value="${new HashMap()}"/>
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" status="counter">
        <% allEmailAddresses = ""; %>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org).prs}" var="person">
                <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                        <% allEmailAddresses += email?.content?.trim() + "; "%>
                    </g:each>
                </g:if>
            </g:each>
        </g:each>
        <% generalContactPrsMap.put(prsFunction.id, allEmailAddresses)%>
    </g:each>
    <div class="ui form">
        <div class="field">
            <g:textArea id="emailAddressesTextArea" name="emailAddresses" readonly="false" rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" />
        </div>
    </div>

    <g:javascript>
        // modals
        $("*[data-semui='modal']").click(function() {
            $($(this).attr('href') + '.ui.modal').modal({
                onVisible: function() {
                    updateTextArea();
                    $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                },
                detachable: true,
                autofocus: false,
                closable: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                }
            }).modal('show')
        });

        var jsonEmailMap = <%=groovy.json.JsonOutput.toJson((Map)generalContactPrsMap)%>;

        $('#roleTypeMultiSelect').change(function() { updateTextArea(); });

        function updateTextArea() {
            var selectedRoleTypIds = $("#roleTypeMultiSelect").val();
            var emailsForSelectedRoleTypes = ""
            for (var i = 0; i<selectedRoleTypIds.length; i++) {
                emailsForSelectedRoleTypes += jsonEmailMap[selectedRoleTypIds[i]]
            }
            $('#emailAddressesTextArea').val(emailsForSelectedRoleTypes);
        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
