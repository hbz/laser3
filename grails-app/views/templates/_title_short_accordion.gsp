<%@ page import="de.laser.storage.RDStore;" %>


    <ui:listIcon type="${tipp.titleType}"/>
    <g:if test="${ie}">
        <g:link controller="issueEntitlement" id="${ie.id}" action="show"><strong>${ie.name}</strong>
        </g:link>
    </g:if>
    <g:else>
        <g:link controller="tipp" id="${tipp.id}" action="show"><strong>${tipp.name}</strong></g:link>
    </g:else>

    <g:if test="${tipp.hostPlatformURL}">
        <ui:linkWithIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
    </g:if>



