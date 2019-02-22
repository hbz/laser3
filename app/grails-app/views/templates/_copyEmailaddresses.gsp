<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail"               value="${RDStore.CCT_EMAIL}"/>
    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues('Person Function')}"/>
    <g:set var="rdvAllPersonPositions"  value="${PersonRole.getAllRefdataValues('Person Position')}"/>

    <div class="field">
        <label><g:message code="person.function.label" default="Function"/></label>&nbsp
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
        <label><g:message code="person.position.label" default="Position"/></label>&nbsp
        <laser:select class="ui dropdown search"
                      name="prsPositionMultiSelect"
                      multiple=""
                      from="${rdvAllPersonPositions}"
                      optionKey="id"
                      optionValue="value"
                      />
    </div>
    <br><br>
    %{--Create Collections of EmailAdresses, that will be shown by javascript acconding to the dropdown selection--}%
    %{--Create a map with EmailAdresses for each Element in the dropdownmenu--}%
    <g:set var="functionEmailsMap" value="${new HashMap()}"/>
    %{--Create a set with all EmailAdresses, in case no dropdown Element is selected--}%
    <g:set var="functionAllEmailsSet" value="${new HashSet()}"/>
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" >
        <g:set var="emailsForFunction" value="${new HashSet()}"/>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org).prs}" var="person">
                <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                        <%  emailsForFunction.add( email?.content?.trim() )
                            functionAllEmailsSet.add( email?.content?.trim() ) %>
                    </g:each>
                </g:if>
            </g:each>
        </g:each>
        <% functionEmailsMap.put(prsFunction.id, emailsForFunction) %>
    </g:each>
    <g:each in="${rdvAllPersonPositions}" var="prsPosition" >
        <g:set var="emailsForPosition" value="${new HashSet()}"/>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByPositionTypeAndOrg(prsPosition, org).prs}" var="person">
                <g:if test="${(person?.isPublic?.value=='Yes') || (person?.isPublic?.value=='No' && person?.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, rdvEmail)}" var="email">
                        <%  emailsForPosition.add( email?.content?.trim() )
                        functionAllEmailsSet.add( email?.content?.trim() ) %>
                    </g:each>
                </g:if>
            </g:each>
        </g:each>
        <% functionEmailsMap.put(prsPosition.id, emailsForPosition)%>
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
        var jsonAllEmailSet = <%=groovy.json.JsonOutput.toJson((Set)functionAllEmailsSet)%>;

        $('#prsFunctionMultiSelect').change(function() { updateTextArea(); });
        $('#prsPositionMultiSelect').change(function() { updateTextArea(); });

        function updateTextArea() {
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );
            var emailsForSelectedRoleTypes = new Array();
            if (selectedRoleTypIds.length == 0) {
                emailsForSelectedRoleTypes = jsonAllEmailSet;
            } else {
                // Collect selected EmailAdresses from Map without duplicates
                for (var i = 0; i<selectedRoleTypIds.length; i++) {
                    var tmpEmailArray = jsonEmailMap[selectedRoleTypIds[i]];
                    for (var j = 0; j<tmpEmailArray.length; j++) {
                        var email = tmpEmailArray[j].trim();
                        if ( ! emailsForSelectedRoleTypes.includes(email)) {
                            emailsForSelectedRoleTypes.push(email);
                        }
                    }
                }
            }
            var emailsAsString = Array.from(emailsForSelectedRoleTypes);
            emailsAsString.sort(function(a, b) {
                return a.toLowerCase().localeCompare(b.toLowerCase());
            });
            emailsAsString = emailsAsString.join('; ');
            $('#emailAddressesTextArea').val(emailsAsString);
        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
