<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${editmode}">
    <a role="button" class="${Btn.SIMPLE}" data-ui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<ui:modal contentClass="scrolling" modalSize="medium" id="${tmplModalID}" text="${tmplText}" isEditModal="${editmode}">
    <div class="ui info message">
        <i class="${Icon.UI.INFO}"></i> ${message(code: 'subscription.details.linkAgency.minTwoLetters')}
    </div>
    <g:form id="create_vendor_role_link_${tmplModalID}" class="ui form" url="[controller:'ajax', action:'addVendorRole']" method="post">
        <input type="hidden" name="parent" value="${parent}" />
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

