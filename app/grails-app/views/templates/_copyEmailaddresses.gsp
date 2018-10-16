<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail" value="${RefdataValue.getByValueAndCategory('E-Mail','ContactContentType')}"/>
    <g:set var="rdvGeneralContactPrs" value="${RefdataValue.getByValueAndCategory('General contact person', 'Person Function')}"/>
    <g:set var="rdvAllPersonFunctions" value="${PersonRole.getAllRefdataValues('Person Function').sort {it.getI10n("value")}}"/>
    <g:set var="emailAddressLists" value="new ArrayList()"/>

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
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" status="counter">
        <% allEmailAddresses = null; %>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org)}" var="person">
                <g:each in ="${Contact.findAllByPrsAndContentType(person.getPrs(), rdvEmail)}" var="email">
                    <% allEmailAddresses = (allEmailAddresses == null)? email?.content.trim() + "; " : allEmailAddresses + email?.content.trim() + "; "; %>
                </g:each>
            </g:each>
        </g:each>
        <g:if test="${prsFunction.id == rdvGeneralContactPrs.id}">

            <g:textArea name="emailaddressfield${counter}" readonly="false" rows="5" cols="1" class="myTargets" style="width: 100%;">${allEmailAddresses}</g:textArea>
        </g:if>
        <g:else>
            <g:textArea name="emailaddressfield${counter}" readonly="false" rows="5" cols="1" class="myTargets hidden" style="width: 100%;">${allEmailAddresses}</g:textArea>
        </g:else>
    </g:each>

    <g:javascript>
        $('#newPrsRoleType').change(function() {
            $('.myTargets').addClass('hidden');

            var ndx = $("#newPrsRoleType").prop('selectedIndex');
            console.log( $('#emailaddressfield' + ndx) )
            $('#emailaddressfield' + ndx).removeClass('hidden');
        });
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
