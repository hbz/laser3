<%@ page import="com.k_int.kbplus.Org" %>
<g:if test="${editmode}">
    <a class="ui button" data-semui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}">
    <g:set var="ajaxID" value="${tmplID ?:'allOrgs'}"/>
    <g:form id="create_org_role_link" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post">
        <input type="hidden" name="parent" value="${parent}" />
        <input type="hidden" name="property" value="${property}" />
        <input type="hidden" name="recip_prop" value="${recip_prop}" />

        <input type="hidden" name="orm_orgRole" value="${tmplRole?.id}" />
        <input type="hidden" name="linkType" value="${linkType}" />

        <div class="field">
            <g:if test="${orgList.size() > 0}">
                <label class="control-label">
                        Bitte aus den ${orgList.size()} verfügbaren Organisation auswählen ..
                </label>
                <g:set var="varSelectOne" value="${message(code:'default.selectOne.label')}" />

                <g:select name="orm_orgOid" class="ui fluid search selection dropdown"
                          noSelection="${['':varSelectOne]}"
                          from="${orgList}"
                          optionKey="${{ Org.class.name + ':' + it.id }}"
                          optionValue="name"
                />
            </g:if>
            <g:else>
                <p>Es wurden leider keine gültigen Organisationen gefunden.</p>
            </g:else>


        </div>
    </g:form>
</semui:modal>

