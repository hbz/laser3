<laser:htmlStart text="Database Migration" />

    <ui:breadcrumbs>
      <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
      <ui:crumb text="Database Migration" class="active"/>
    </ui:breadcrumbs>

    <ui:messages data="${flash}" />

    <ui:h1HeaderWithIcon text="Private Properties (mandatory) without existing values" />

    <g:link controller="yoda" action="dbmFixPrivateProperties" params="[cmd:'doIt']" class="ui button negative">
        <i class="icon trash alternate outline"></i> &nbsp;  Delete all</g:link>

    <br />

    <table class="ui table compact">
        <tbody>
        <g:each in="${candidates}" var="cat, ppp">
            <tr>
                <td colspan="5">
                    <h3 class="ui header">${cat} - ${ppp.size()}</h3>
                </td>
            </tr>
            <tr>
                <td><strong>id</strong></td>
                <td><strong>pd.id</strong></td>
                <td></td>
                <td><strong>owner.id</strong></td>
                <td></td>
            </tr>
            <g:each in="${ppp}" var="pp">
                <tr>
                    <td>${pp.id}</td>
                    <td>${pp.type.id}</td>
                    <td>${pp.type.name}</td>
                    <td>${pp.owner.id}</td>
                    <td>${pp.owner }</td>
                </tr>
            </g:each>
        </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
