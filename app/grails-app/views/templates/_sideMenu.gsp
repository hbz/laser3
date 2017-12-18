<!-- sideMenu::A -->
    <semui:card text="${entityName}" class="card-grey">
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
    </semui:card>
<!-- sideMenu::O -->
