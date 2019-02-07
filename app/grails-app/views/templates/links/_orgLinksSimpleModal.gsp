<%@ page import="com.k_int.kbplus.Org" %>
<g:if test="${editmode}">
    <a class="ui button" data-semui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}" editmodal="${editmode}">
    <g:form id="create_org_role_link" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post">
        <input type="hidden" name="parent" value="${parent}" />
        <input type="hidden" name="property" value="${property}" />
        <input type="hidden" name="recip_prop" value="${recip_prop}" />
        <input type="hidden" name="orm_orgRole" value="${tmplRole?.id}" />
        <input type="hidden" name="linkType" value="${linkType}" />

        <div class="field">
            <g:if test="${orgList.size() > 0}">
                <p>
                    Es wurden ${orgList.size()} verfügbare ${tmplEntity} gefunden.
                    <br />
                    Bereits von Ihnen verwendete ${tmplEntity} sind durch ein Symbol (&#10004;) gekennzeichnet.
                </p>
                <g:set var="varSelectOne" value="${message(code:'default.selectOne.label')}" />

                <semui:signedDropdown name="orm_orgOid" noSelection="${varSelectOne}" from="${orgList}" signedIds="${signedIdList}" />
            </g:if>
            <g:else>
                <p>Es wurden leider keine gültigen ${tmplEntity} gefunden.</p>
            </g:else>
        </div>
    </g:form>
</semui:modal>

