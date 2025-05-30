<laser:htmlStart message="menu.admin.deletedObjects" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.deletedObjects" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.deletedObjects" type="admin"/>

<div class="ui fluid card">
    <div class="content">

        <table class="ui la-table compact table">
            <thead>
                <tr>
                    <th>Objekt</th>
                    <th>${message(code:'default.count.label')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${stats}" var="row">
                    <tr>
                        <td>${row.key}</td>
                        <td>${row.value[0]}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>
</div>

<laser:htmlEnd />
