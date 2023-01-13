<%@ page import="de.laser.storage.RDStore;" %>

<div class="ui list">
    <g:each in="${tipp.ids.sort { it.ns.ns }}" var="title_id">
        <span class="item js-copyTriggerParent">
            <span class="ui small basic image label js-copyTrigger la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'tooltip.clickToCopy', args: [title_id.ns.ns])}">
                <i class="la-copy grey icon la-js-copyTriggerIcon"></i>
                ${title_id.ns.ns}: <div class="detail js-copyTopic">${title_id.value}</div>
            </span>
        </span>
    </g:each>
</div>
