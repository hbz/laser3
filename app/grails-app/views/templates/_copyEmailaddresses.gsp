<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<%@ page import="static de.laser.helper.RDStore.*" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyEmailaddresses_ajaxModal'}"/>

<semui:modal id="${modalID ?: 'copyEmailaddresses_ajaxModal'}" text="${message(code:'menu.institutions.copy_emailaddresses', args:[orgList?.size()?:0])}" hideSubmitButton="true">

    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues(de.laser.helper.RDConstants.PERSON_FUNCTION)}" scope="request"/>
    <g:set var="rdvAllPersonPositions"  value="${PersonRole.getAllRefdataValues(de.laser.helper.RDConstants.PERSON_POSITION)}" scope="request"/>

    <div>
    <label><g:message code="person.function.label" default="Function"/></label>&nbsp
        <laser:select class="ui dropdown search"
                      name="prsFunctionMultiSelect"
                      multiple=""
                      from="${rdvAllPersonFunctions}"
                      optionKey="id"
                      optionValue="value"
                      value="${PRS_FUNC_GENERAL_CONTACT_PRS.id}"/>
    </div>
    <br>
    <div>
        <label><g:message code="person.position.label" default="Position"/></label>&nbsp
        <laser:select class="ui dropdown search"
                      name="prsPositionMultiSelect"
                      multiple=""
                      from="${rdvAllPersonPositions}"
                      optionKey="id"
                      optionValue="value"
                      />
    </div>
    <br />
    <div class="ui checkbox">
        <input type="checkbox" id="publicContacts" checked/>
        <label for="publicContacts">${message(code:'email.fromPublicContacts')}</label>
    </div>
    <div class="ui checkbox">
        <input type="checkbox" id="privateContacts" checked/>
        <label for="privateContacts">${message(code:'email.fromPrivateAddressbook')}</label>
    </div>

    <br><br>
    %{--Create Collections of EmailAdresses, that will be shown by javascript acconding to the dropdown selection--}%
    %{--Create a map with EmailAdresses for each Element in the dropdownmenu--}%
    <g:set var="functionPublicEmailsMap" value="${new HashMap()}"/>
    <g:set var="functionPrivateEmailsMap" value="${new HashMap()}"/>
    %{--Create a set with all EmailAdresses, in case no dropdown Element is selected--}%
    <g:set var="functionAllPublicEmailsSet" value="${new HashSet()}"/>
    <g:set var="functionAllPrivateEmailsSet" value="${new HashSet()}"/>
    <g:each in="${rdvAllPersonFunctions}" var="prsFunction" >
        <g:set var="publicEmailsForFunction" value="${new HashSet()}"/>
        <g:set var="privateEmailsForFunction" value="${new HashSet()}"/>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(prsFunction, org).prs}" var="person">
                <g:if test="${person?.isPublic}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, CCT_EMAIL)}" var="email">
                        <%
                            def emailPF = email?.content?.trim()
                            if (emailPF != null) {
                                publicEmailsForFunction.add( emailPF )
                                functionAllPublicEmailsSet.add( emailPF )
                            }
                        %>
                    </g:each>
                </g:if>
                <g:elseif test="${(! person.isPublic) && (person.tenant?.id == contextService.getOrg()?.id)}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, CCT_EMAIL)}" var="email">
                        <%
                            emailPF = email?.content?.trim()
                            if (emailPF != null) {
                                privateEmailsForFunction.add( emailPF )
                                functionAllPrivateEmailsSet.add( emailPF )
                            }
                        %>
                    </g:each>
                </g:elseif>
            </g:each>
 %{--comment in for control purposes--}%
            %{--<g:if test="${privateEmailsForFunction || publicEmailsForFunction}">--}%
                %{--<g:textArea id="test1" name="test1" rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" >--}%
                    %{--${"---------privateEmailsForFunction for Org "+org.sortname+" and function "+prsFunction?.getI10n('value')+"-----"}--}%
                    %{--${privateEmailsForFunction}--}%
                    %{--${"---------publiceEmailsForFunction for Org "+org.sortname+" and function "+prsFunction?.getI10n('value')+"-----"}--}%
                    %{--${publicEmailsForFunction}--}%
                %{--</g:textArea>--}%
            %{--</g:if>--}%
        </g:each>
        <%
            functionPublicEmailsMap.put(prsFunction.id, publicEmailsForFunction)
            functionPrivateEmailsMap.put(prsFunction.id, privateEmailsForFunction)
        %>
    </g:each>
    <g:each in="${rdvAllPersonPositions}" var="prsPosition" >
        <g:set var="publicEmailsForPosition" value="${new HashSet()}"/>
        <g:set var="privateEmailsForPosition" value="${new HashSet()}"/>
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByPositionTypeAndOrg(prsPosition, org).prs}" var="person">
                <g:if test="${person?.isPublic}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, CCT_EMAIL)}" var="email">
                        <%
                            def emailPP = email?.content?.trim()
                            if (emailPP != null) {
                                publicEmailsForPosition.add(emailPP)
                                functionAllPublicEmailsSet.add(emailPP)
                            }
                        %>
                    </g:each>
                </g:if>
                <g:elseif test="${(! person.isPublic) && person.tenant?.id == contextService.getOrg()?.id}">
                    <g:each in ="${Contact.findAllByPrsAndContentType(person, CCT_EMAIL)}" var="email">
                        <%
                            emailPP = email?.content?.trim()
                            if (emailPP != null) {
                                privateEmailsForPosition.add(emailPP)
                                functionAllPrivateEmailsSet.add(emailPP)
                            }
                        %>
                    </g:each>
                </g:elseif>
            </g:each>
        </g:each>
        <%
            functionPublicEmailsMap.put(prsPosition.id, publicEmailsForPosition)
            functionPrivateEmailsMap.put(prsPosition.id, privateEmailsForPosition)
        %>
    </g:each>
    <div class="ui form">
        <div class="field">
            <g:textArea id="emailAddressesTextArea" name="emailAddresses" readonly="false" rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" />
        </div>
        <button class="ui icon button right floated" onclick="copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br>
    </div>

    <g:javascript>
        // modals
        $("*[data-semui='modal']").click(function() {

            var href = $(this).attr('data-href')
            if (! href) {
                href = $(this).attr('href')
            }
            $(href + '.ui.modal').modal({
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

        var jsonFunctionPublicEmailsMap = <%=groovy.json.JsonOutput.toJson((Map)functionPublicEmailsMap)%>;
        var jsonFunctionPrivateEmailsMap = <%=groovy.json.JsonOutput.toJson((Map)functionPrivateEmailsMap)%>;
        var jsonFunctionAllPublicEmailsSet = <%=groovy.json.JsonOutput.toJson((Set)functionAllPublicEmailsSet)%>;
        var jsonFunctionAllPrivateEmailsSet = <%=groovy.json.JsonOutput.toJson((Set)functionAllPrivateEmailsSet)%>;

        $("#prsFunctionMultiSelect").change(function() { updateTextArea(); });
        $("#prsPositionMultiSelect").change(function() { updateTextArea(); });
        $("#privateContacts").change(function() { updateTextArea(); });
        $("#publicContacts").change(function() { updateTextArea(); });

        function copyToEmailProgram() {
            var emailAdresses = $("#emailAddressesTextArea").val();
            window.location.href = "mailto:"+emailAdresses;
        }

        function copyToClipboard() {
            $("#emailAddressesTextArea").select();
            document.execCommand("copy");
        }

        function updateTextArea() {
            var isPrivate = $("#privateContacts").is(":checked")
            var isPublic = $("#publicContacts").is(":checked")
            $("#emailAddressesTextArea").val("")
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );
            var emailsForSelectedRoleTypes = new Array();
            if (selectedRoleTypIds.length == 0) {
                if (isPrivate) emailsForSelectedRoleTypes.pushValues(jsonFunctionAllPrivateEmailsSet);
                if (isPublic) emailsForSelectedRoleTypes.pushValues(jsonFunctionAllPublicEmailsSet);
            } else {
                // Collect selected EmailAdresses from Map without duplicates
                for (var i = 0; i<selectedRoleTypIds.length; i++) {
                    if (isPrivate){
                        var tmpEmailArray = jsonFunctionPrivateEmailsMap[selectedRoleTypIds[i]];
                        for (var j = 0; j<tmpEmailArray.length; j++) {
                            var email = tmpEmailArray[j].trim();
                            if ( ! emailsForSelectedRoleTypes.includes(email)) {
                                emailsForSelectedRoleTypes.push(email);
                            }
                        }
                    }
                    if (isPublic){
                        var tmpEmailArray = jsonFunctionPublicEmailsMap[selectedRoleTypIds[i]];
                        for (var j = 0; j<tmpEmailArray.length; j++) {
                            var email = tmpEmailArray[j].trim();
                            if ( ! emailsForSelectedRoleTypes.includes(email)) {
                                emailsForSelectedRoleTypes.push(email);
                            }
                        }
                    }
                }
            }
            var emailsAsString = Array.from(emailsForSelectedRoleTypes);
            emailsAsString.sort(function(a, b) {
                return a.toLowerCase().localeCompare(b.toLowerCase());
            });
            emailsAsString = emailsAsString.join('; ');
            $("#emailAddressesTextArea").val(emailsAsString);
        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
