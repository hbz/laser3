<%@ page import="de.laser.storage.RDStore;" %>
<%@ page import="de.laser.IdentifierNamespace;" %>


<div class="ui list">
    <g:each in="${tipp.ids?.sort{it?.ns?.ns}}" var="title_id">
        <g:if test="${!(actionName == 'findOrganisationMatches' && (title_id.value == '' || title_id.value == IdentifierNamespace.UNKNOWN))}">
            <span class="item js-copyTriggerParent">
                <span class="ui small basic image label js-copyTrigger la-popup-tooltip la-delay la-js-notOpenAccordion" data-position="top center" data-content="${message(code: 'tooltip.clickToCopy', args: [title_id.ns.ns])}">
                    <i class="la-copy grey icon la-js-copyTriggerIcon"></i>
                    ${title_id.ns.ns}: <span class="detail js-copyTopic">${title_id.value}</span>
                </span>
            </span>
        </g:if>
    </g:each>
</div>
