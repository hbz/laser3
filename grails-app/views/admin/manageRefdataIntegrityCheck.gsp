<laser:htmlStart message="default.integrityCheck.label" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index" />
        <ui:crumb message="menu.admin.manageRefdatas" controller="admin" action="manageRefdatas" />
        <ui:crumb message="default.integrityCheck.label" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="default.integrityCheck.label" type="admin"/>

    <ui:messages data="${flash}" />

    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th>Class.Field</th>
            <th>Annotation</th>
            <th>${message(code:'default.integrityCheck.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${integrityCheck}" var="cls">
            <g:each in="${cls.value}" var="entry">
                <tr>
                    <td>
                        <strong>${cls.key}</strong>.${entry.field}
                    </td>
                    <td>
                        ${entry.cat}
                    </td>
                    <td>
                    <!-- ${entry.rdc} -->
                        <g:each in="${entry.check}" var="check">
                            <g:if test="${check.value != true}">
                                <span class="ui label red la-popup-tooltip la-delay" data-content="RDC: ${check.value.desc} (${check.value.id})">${check.key}</span>
                            </g:if>
                            <g:else>
                                <span style="padding:0 0.5em 0.5em 0">${check.key}</span>
                            </g:else>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
