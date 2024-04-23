<%@ page import="de.laser.storage.RDStore;" %>
<ui:exportDropdown>
    <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: '']"/>
</ui:exportDropdown>

<g:render template="/clickMe/export/js"/>

