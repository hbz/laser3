<semui:breadcrumbs>
    <g:if test="${orgInstance.orgType == com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'OrgType')}">
        <semui:crumb controller="organisations" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <semui:crumb message="" class="active"/>
    </g:if>
    <g:else>
        <semui:crumb controller="organisations" action="show" id="${orgInstance.id}" text="${orgInstance.name}" />
        <semui:crumb message="menu.institutions.addressbook" class="active"/>
    </g:else>
</semui:breadcrumbs>