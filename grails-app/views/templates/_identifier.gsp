<%@ page import="de.laser.storage.RDStore;" %>

<div class="ui list js-copyTriggerParent">
    <g:each in="${tipp.ids.sort { it.ns.ns }}" var="title_id">
        <div class="item">
            <span class="ui small basic image label js-copyTrigger js-copyTopic la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'tooltip.clickToCopy', args: [title_id.ns.ns])}">
            <i class="la-copy icon la-js-copyTriggerIcon"></i>

                ${title_id.ns.ns}: <div class="detail">${title_id.value}</div>
            </span>
        </div>
    </g:each>
</div>
