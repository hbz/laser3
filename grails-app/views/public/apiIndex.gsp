<laser:htmlStart text="${message(code: 'apiRelease')}" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon text="${message(code: 'apiRelease')}" type="help"/>

<div class="ui secondary pointing menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="api" id="${h}" class="item">${h != 'legacy' ? 'Version: ' + h : 'Archiv'}</g:link>
    </g:each>

    <g:link controller="public" action="releases" class="item right floated"><icon:arrow /> ${message(code:'releaseNotes')}</g:link>
</div>

<div class="ui segment">
    TBD
</div>

<laser:htmlEnd />