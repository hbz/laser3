<laser:serviceInjection />

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.admin')} <i class="dropdown icon"></i>
    </a>

    <div class="menu" role="menu">
        <ui:link addItemAttributes="true" controller="admin" action="index">${message(code:'default.dashboard')}</ui:link>
        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                <i class="ui icon keyboard outline"></i> ${message(code:'menu.admin.sysAdmin')} <i class="dropdown icon"></i>
            </div>

            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="admin" action="appInfo">${message(code:'menu.admin.appInfo')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</ui:link>

                <div class="divider"></div>
                <ui:link addItemAttributes="true" controller="admin" action="testMailSending">Test Mail Sending</ui:link>
            </div>
        </div>

        <ui:link addItemAttributes="true" controller="admin" action="systemMessages"><i class="icon exclamation circle"></i>${message(code: 'menu.admin.systemMessage')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="systemAnnouncements"><i class="icon flag"></i>${message(code:'menu.admin.announcements')}</ui:link>

        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                ${message(code:'org.plural.label')} <i class="dropdown icon"></i>
            </div>

            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</ui:link>
            </div>
        </div>

        <ui:link addItemAttributes="true" controller="user" action="list">${message(code:'menu.institutions.users')}</ui:link>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</ui:link>

        <div class="divider"></div>
        <!-- TODO: workflows-permissions -->
        %{-- <ui:link addItemAttributes="true" controller="admin" action="manageWorkflows">${message(code:'menu.admin.manageWorkflows')}</ui:link> --}%
        <ui:link addItemAttributes="true" controller="usage">${message(code:'menu.admin.manageUsageStats')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="listMailTemplates">${message(code:'menu.admin.manageMailTemplates')}</ui:link>

        <div class="divider"></div>
        <ui:link addItemAttributes="true" controller="admin" action="missingPermantTitlesInSubs">${message(code:'menu.admin.missingPermantTitlesInSubs')}</ui:link>
        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                <i class="ui icon code"></i> <span class="text">Developer</span> <i class="dropdown icon"></i>
            </div>

            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="dev" action="frontend">Frontend</ui:link>
                <ui:link addItemAttributes="true" controller="dev" action="klodav">klodav</ui:link>
            </div>
        </div>
    </div>
</div>