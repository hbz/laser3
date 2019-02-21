<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail"               value="${RDStore.CCT_EMAIL}"/>
    <g:set var="rdvGeneralContactPrs"   value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS}"/>
    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues('Person Function')}"/>
    <g:set var="rdvAllPersonPositions"  value="${PersonRole.getAllRefdataValues('Person Position')}"/>

    <div class="field">
        <label>Funktion</label>
        <laser:select class="ui dropdown search"
                      name="prsFunctionMultiSelect"
                      multiple=""
                      from="${rdvAllPersonFunctions}"
                      optionKey="id"
                      optionValue="value"
                      />
    </div>
    <br>
    <div class="field">
        <label>Position</label>
        <laser:select class="ui dropdown search"
                      name="prsPositionMultiSelect"
                      multiple=""
                      from="${rdvAllPersonPositions}"
                      optionKey="id"
                      optionValue="value"
                      />
    </div>
    <br><br>
    <g:set var="functionEmailsMap" value="${new HashMap()}"/>
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" status="counter">
        <% Set allEmailAddresses = new TreeSet(); %>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org).prs}" var="person">
                <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                        <% allEmailAddresses.add( email?.content?.trim() )%>
                    </g:each>
                </g:if>
            </g:each>
        </g:each>
        <% functionEmailsMap.put(prsFunction.id, allEmailAddresses)%>
    </g:each>
    <g:each in="${rdvAllPersonPositions}" var="prsPosition" status="counter">
        <% allEmailAddresses = new TreeSet(); %>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByPositionTypeAndOrg(prsPosition, org).prs}" var="person">
                <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                        <% allEmailAddresses.add( email?.content?.trim() )%>
                    </g:each>
                </g:if>
            </g:each>
        </g:each>
        <% functionEmailsMap.put(prsPosition.id, allEmailAddresses)%>
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

        var jsonEmailMap = <%=groovy.json.JsonOutput.toJson((Map)functionEmailsMap)%>;

        $('#prsFunctionMultiSelect').change(function() { updateTextArea(); });
        $('#prsPositionMultiSelect').change(function() { updateTextArea(); });

        function updateTextArea() {
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val();
            var selectedRoleTypIds = selectedRoleTypIds.concat( $("#prsPositionMultiSelect").val() );
            var emailsForSelectedRoleTypes = new Array();
            for (var i = 0; i<selectedRoleTypIds.length; i++) {
                var selRoleType = selectedRoleTypIds[i];
                var tmpEmailArray = jsonEmailMap[selRoleType];
                for (var j = 0; j<tmpEmailArray.length; j++) {
                    var email = tmpEmailArray[j].trim();
                    console.log("Liste: " + emailsForSelectedRoleTypes);
                    console.log("Gibts mich schon? " + email + "? " + emailsForSelectedRoleTypes.includes(email));
                    if ( ! emailsForSelectedRoleTypes.includes(email)) {
                        emailsForSelectedRoleTypes.push(email);
                    } else {
                        console.log("Duplikat gefunden: " + email);
                    }
                }
            }
            var emailsAsString = Array.from(emailsForSelectedRoleTypes).sort().join('; ');
            $('#emailAddressesTextArea').val(emailsAsString);
        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
