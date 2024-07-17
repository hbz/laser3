<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Org" %>
<g:if test="${editmode}">
    <a role="button" class="${Btn.SIMPLE}" data-ui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<ui:modal contentClass="scrolling" modalSize="medium" id="${tmplModalID}" text="${tmplText}" isEditModal="${editmode}">
    <div class="ui info message">
        <i class="${Icon.UI.INFO}"></i> ${message(code: 'subscription.details.linkProvider.minTwoLetters')}
    </div>
    <g:form id="create_org_role_link_${tmplModalID}" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post">
        <input type="hidden" name="parent" value="${parent}" />
        <input type="hidden" name="property" value="${property}" />
        <input type="hidden" name="recip_prop" value="${recip_prop}" />
        <input type="hidden" name="orm_orgRole" value="${tmplRole?.id}" />
        <input type="hidden" name="linkType" value="${linkType}" />

        <label for="${tmplModalID}_orgSearch">${message(code: 'title.search')}</label>
        <input type="text" name="orgSearch" id="${tmplModalID}_orgSearch"/>
        <div class="la-clear-before la-padding-top-1em" id="${tmplModalID}_providerResultWrapper">

        </div>
        <%--
        <div class="field">
            <div class="ui search selection dropdown la-full-width" id="orm_orgOid_${tmplModalID}">
                <input type="hidden" name="orm_orgOid"/>
                <i class="dropdown icon"></i>
                <input type="text" class="search"/>
                <div class="default text"></div>
            </div>
        </div>
        --%>
    </g:form>
    <laser:script file="${this.getGroovyPageFileName()}">
        var searchTimer = null;
        var minLength = 2;
        var searchDelay = 300;
        $('#${tmplModalID}_orgSearch').on('input', function() {
            clearTimeout(searchTimer);
            var searchVal = $(this).val();
            if(searchVal.length < minLength) {
                return;
            }
            searchTimer = setTimeout(function() {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="lookupProviders"/>?tableView=true&query="+searchVal,
                    success: function (data) {
                        $('#${tmplModalID}_providerResultWrapper').html(data);
                    }
                });
            }, searchDelay);
        });
    </laser:script>
</ui:modal>

