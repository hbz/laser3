<laser:htmlStart message="menu.admin.deletedObjects" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.deletedObjects" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.deletedObjects" />

<div class="ui grid">
    <div class="twelve wide column">

        <table class="ui celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Objekt</th>
                    <th>Anzahl</th>
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
