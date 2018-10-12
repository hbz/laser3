<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<laser:serviceInjection />

<semui:modal id="copyEmailaddresses_ajaxModal" text="${message(code:'menu.institutions.copy_emailaddresses')}" hideSubmitButton="true">

    <g:set var="rdvEmail" value="${RefdataValue.getByValueAndCategory('E-Mail','ContactContentType')}"/>
    <g:set var="rdvGeneralContactPrs" value="${RefdataValue.getByValueAndCategory('General contact person', 'Person Function')}"/>

    <div class="field">
        <label>Funktion</label>
        <laser:select class="ui dropdown search"
                      name="newPrsRoleType"
                      from="${PersonRole.getAllRefdataValues('Person Function')}"
                      optionKey="id"
                      optionValue="value"
                      value="${rdvGeneralContactPrs.getI10n("value")}"/>
    </div>
    <hr>
    TODO: Voreingestellter Wert: Hauptkontakt und das reagieren auf die Änderung dieser Auswahl fehlen noch
    <br><br>
    Orgs: ${orgList}<br>

    <hr>
    %{--<g:textArea name="emailadressen" readonly="false" rows="1" cols="1" style="width:95%">--}%
        <g:each in="${orgList}" var="org">
            <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(rdvGeneralContactPrs, org)}" var="person">
                <g:each in ="${Contact.findAllByPrsAndContentType(person.getPrs(), rdvEmail)}" var="email">
                    %{--${email?.content};--}%
                    <% allEmailAddresses = (allEmailAddresses == null)? email?.content + "; " : allEmailAddresses + email?.content + "; "; %>
                </g:each>
            </g:each>
        </g:each>
    %{--</g:textArea>--}%
    <g:textArea name="emailadressen1" readonly="false" rows="5" cols="1" style="width: 100%;%">
        ${allEmailAddresses}
    </g:textArea>
    %{--<button class="ui button">--}%
        %{--Kopieren--}%
    %{--</button>--}%
    %{--<button class="ui button">--}%
        %{--E-Mailen--}%
    %{--</button>--}%
    %{--<%--}%
        %{--Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();--}%
        %{--clipboard.setContents(allEmailAddresses, null);--}%
    %{--%>--}%
    %{--<span data-position="right center" data-tooltip="Mail senden an ${allEmailAddresses}">--}%
        %{--<a href="mailto:${allEmailAddresses}" >--}%
            %{--<i class="ui icon envelope outline"></i>--}%
        %{--</a>--}%
    %{--</span>--}%

    <g:javascript>
        $('#newPrsRoleType').change(function() {
            alert("Du hat einen anderen Wert gewählt!");
        });
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
