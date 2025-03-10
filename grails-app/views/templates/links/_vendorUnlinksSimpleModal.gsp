<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${editmode}">
    <a role="button"
       class="ui icon negative button la-modern-button la-popup-tooltip ${tmplCss}"
       data-ui="modal"
       href="#${tmplModalID}"
       data-content="${tmplTooltip}">
        <g:if test="${tmplIcon}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        <g:if test="${tmplButtonText}">
            ${tmplButtonText}
        </g:if>
    </a>
</g:if>

<ui:modal contentClass="scrolling" modalSize="medium" id="${tmplModalID}" text="${tmplText}" isEditModal="${editmode}">
    <div class="ui info message">
        <i class="${Icon.UI.INFO}"></i> ${message(code: 'subscription.details.linkAgency.minTwoLetters')}
    </div>
    <g:form id="create_vendor_role_unlink_${tmplModalID}" class="ui form" url="[controller:'ajax', action:'delAllVendorRoles']" method="post">
        <g:if test="${parent}">
            <input type="hidden" name="parent" value="${parent}" />
        </g:if>
        <g:if test="${myInstitutionController}">
            <input type="hidden" name="takeSelectedSubs" value="/${controllerName}/subscriptionManagement/${params.tab}/${user.id}" />
            <input type="hidden" name="membersListToggler" class="membersListToggler_modal" value="false" />
        </g:if>
        <input type="hidden" name="recip_prop" value="${recip_prop}" />

        <label for="${tmplModalID}_vendorSearch">${message(code: 'title.search')}</label>
        <input type="text" name="vendorSearch" id="${tmplModalID}_vendorSearch"/>
        <div class="la-clear-before la-padding-top-1em" id="${tmplModalID}_vendorResultWrapper">

        </div>
    </g:form>
    <laser:script file="${this.getGroovyPageFileName()}">
        var searchTimer = null;
        var minLength = 2;
        var searchDelay = 300;
        $('#${tmplModalID}_vendorSearch').on('input', function() {
            clearTimeout(searchTimer);
            var searchVal = $(this).val();
            if(searchVal.length < minLength) {
                return;
            }
            searchTimer = setTimeout(function() {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="lookupVendors"/>?tableView=true&query="+searchVal,
                    success: function (data) {
                        $('#${tmplModalID}_vendorResultWrapper').html(data);
                    }
                });
            }, searchDelay);
        });
    </laser:script>
</ui:modal>

