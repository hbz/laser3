<%@ page import="de.laser.Org" %>
<g:if test="${editmode}">
    <a role="button" class="ui button" data-semui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}" isEditModal="${editmode}">
    <g:form id="create_org_role_link_${tmplModalID}" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post">
        <input type="hidden" name="parent" value="${parent}" />
        <input type="hidden" name="property" value="${property}" />
        <input type="hidden" name="recip_prop" value="${recip_prop}" />
        <input type="hidden" name="orm_orgRole" value="${tmplRole?.id}" />
        <input type="hidden" name="linkType" value="${linkType}" />

        <div class="field">
            <div class="ui search selection dropdown la-full-width" id="orm_orgOid_${tmplModalID}">
                <input type="hidden" name="orm_orgOid"/>
                <i class="dropdown icon"></i>
                <input type="text" class="search"/>
                <div class="default text"></div>
            </div>
        </div>
    </g:form>
    <laser:script file="${this.getGroovyPageFileName()}">
            //{query} is correct; this is the semantic ui query syntax containing the filter string
            $("#orm_orgOid_${tmplModalID}").dropdown({
                apiSettings: {
                    url: "<g:createLink controller="ajaxJson" action="lookupProvidersAgencies"/>?&query={query}",
                    cache: false
                },
                clearable: true,
                minCharacters: 1
            });
    </laser:script>
</semui:modal>

