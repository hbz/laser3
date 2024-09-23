<%@ page import="de.laser.storage.RDStore" %>

<g:if test="${institutionalView}">
    <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
</g:if>
<g:else>
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
</g:else>

<laser:htmlStart message="${'menu.institutions.org.show'}" serviceInjection="true"/>

<laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>

<ui:h1HeaderWithIcon text=" Mail-Infos: ${orgInstance.name}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>

<div class="ui stackable grid">
    <div class="sixteen wide column">
        <div class="ui form">

            <g:form controller="organisation" action="mailInfos" params="[subscription: sub.id, id: orgInstance.id]">
                <div class="field">
               <label for="newLanguage">${message(code: 'profile.language')}</label>
                    <select id="newLanguage" name="newLanguage" class="ui search selection fluid dropdown" onchange="this.form.submit()">
                        <g:each in="${[RDStore.LANGUAGE_DE, RDStore.LANGUAGE_EN]}" var="lan">
                            <option <%=language==lan.value ? 'selected="selected"' : ''%> value="${lan.value}">${lan.getI10n('value')}</option>
                        </g:each>
                    </select>
                </div>
            </g:form>

            <div class="field">
                <label for="mailAddressOfProvider">${message(code: 'provider.label')}: E-Mails</label>

                <input type="text" name="mailAddressOfProvider" id="mailAddressOfProvider" readonly="readonly" value="${mailAddressOfProvider}"/>
            </div>


            <div class="field">
                <label for="mailText">${message(code: 'mail.sendMail.mailText')}</label>
                <g:textArea id="emailText" name="mailText" rows="30" cols="1"
                            style="width: 100%;">${mailText}</g:textArea>
            </div>

        </div>
    </div>
</div>

<laser:htmlEnd/>