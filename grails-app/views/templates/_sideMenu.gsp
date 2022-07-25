<!-- sideMenu -->
    <ui:card text="${entityName}">
        <div class="content">
        <ul class="nav nav-list">
            <li>
                <g:link action="list">
                    <i class="icon-list"></i>
                    <g:message code="default.list"  />
                </g:link>
            </li>
            <li>
                <g:link action="create">
                    <i class="icon-plus"></i>
                    <g:message code="default.create.label" args="[entityName]" />
                </g:link>
            </li>
        </ul>
        </div>
    </ui:card>
<!-- sideMenu -->
