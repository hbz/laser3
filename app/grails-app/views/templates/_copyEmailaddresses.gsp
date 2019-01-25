<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail"               value="${RDStore.CCT_EMAIL}"/>
    <g:set var="rdvGeneralContactPrs"   value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS}"/>
    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues('Person Function')}"/>

    <div class="field">
        <label>Funktion</label>
        <laser:select class="ui dropdown search"
                      name="newPrsRoleType"
                      from="${rdvAllPersonFunctions}"
                      optionKey="id"
                      optionValue="value"
                      value="${rdvGeneralContactPrs.id}"/>
    </div>
    <br><br>
    %{--TextAreas für alle PersonFunctions anlegen und je nach Dropdownauswahl anzeigen--}%
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" status="counter">
        <% allEmailAddresses = ""; %>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org).prs}" var="person">
                <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                    <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                        <% allEmailAddresses += email?.content?.trim() + "; "%>
                    </g:if>
                </g:each>
            </g:each>
        </g:each>
        <g:if test="${prsFunction.id == rdvGeneralContactPrs.id}">
            <div class="ui form">
                <div class="field">
                    <g:textArea name="emailaddressfield${counter}" readonly="false" rows="5" cols="1" class="myTargets">${allEmailAddresses}</g:textArea>
                </div>
            </div>
        </g:if>
        <g:else>
            <g:textArea name="emailaddressfield${counter}" readonly="false" rows="5" cols="1" class="myTargets hidden" style="width: 100%;">${allEmailAddresses}</g:textArea>
        </g:else>
    </g:each>

    <g:javascript>
        $('#newPrsRoleType').change(function() {
            $('.myTargets').addClass('hidden');
            var ndx = $("#newPrsRoleType").prop('selectedIndex');
            $('#emailaddressfield' + ndx).removeClass('hidden');
        });
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
