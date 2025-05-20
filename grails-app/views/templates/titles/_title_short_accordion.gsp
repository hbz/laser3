<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.remote.Wekb" %>
<div class="item">
    <ui:listIcon type="${tipp.titleType}"/>

    <g:if test="${isPublic_gascoDetails}">
        <strong>${ie ? ie.tipp.name : tipp.name}</strong>
    </g:if>
    <g:else>%{-- else=default --}%
        <g:if test="${ie}">
            <g:link controller="issueEntitlement" id="${ie.id}" action="show"><strong>${ie.tipp.name}</strong></g:link>
        </g:if>
        <g:else>
            <g:link controller="tipp" id="${tipp.id}" action="show" params="[sub: sub]"><strong>${tipp.name}</strong></g:link>
        </g:else>
    </g:else>

    <g:if test="${tipp.hostPlatformURL}">
        <ui:linkWithIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
    </g:if>
</div>

<g:if test="${(tipp.titleType == 'monograph') && (tipp.editionStatement || showEmptyFields)}">
    <div class="item">
        <i class="grey ${Icon.CMD.COPY} la-popup-tooltip"
           data-content="${message(code: 'title.editionStatement.label')}"></i>

        <div class="content">
            <div class="description">
                ${message(code: 'title.editionStatement.label') + ':'} ${tipp.editionStatement}
            </div>
        </div>
    </div>
</g:if>
<div class="item">
    <g:if test="${controllerName != 'tipp' && tipp.id}">

        <g:if test="${isPublic_gascoDetails}">
        </g:if>
        <g:else>%{-- else=default --}%
            <g:link class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
                    data-content="${message(code: 'laser')}"
                    target="_blank"
                    controller="tipp" action="show" id="${tipp.id}">
                <i class="${Icon.TIPP}"></i>
            </g:link>
        </g:else>
    </g:if>

        <g:if test="${tipp.gokbId}">
            <a role="button" class="${Btn.ICON.SIMPLE_TOOLTIP} tiny"
               data-content="${message(code: 'wekb')}"
               href="${Wekb.getURL() + '/public/tippContent/?id=' + tipp.gokbId}"
               target="_blank"><i class="${Icon.WEKB}"></i>
            </a>
        </g:if>

</div>
