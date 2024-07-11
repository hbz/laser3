<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Various" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item active">Various</g:link>
    <g:link controller="dev" action="icons" class="item"><i class="certificate icon red"></i> New Icons</g:link>
</nav>

<div class="ui four column grid">
    <div class="column">
        <div class="ui icon info message">
            <i class="${Icons.UI.INFO}"></i>
            <div class="content"> INFO </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon warning message">
            <i class="${Icons.UI.WARNING}"></i>
            <div class="content"> WARNING </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon success message">
            <i class="${Icons.UI.SUCCESS}"></i>
            <div class="content"> SUCCESS </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon error message">
            <i class="${Icons.UI.ERROR}"></i>
            <div class="content"> ERROR </div>
        </div>
    </div>
</div>

<div class="ui six column grid">
    <div class="column">
        <div class="ui info message">
            <div class="content"> INFO </div>
        </div>
    </div>
    <div class="column">
        <div class="ui warning message">
            <div class="content"> WARNING </div>
        </div>
    </div>
    <div class="column">
        <div class="ui success message">
            <div class="content"> SUCCESS </div>
        </div>
    </div>
    <div class="column">
        <div class="ui error message">
            <div class="content"> ERROR </div>
        </div>
    </div>
    <div class="column">
        <div class="ui positive message">
            <div class="content"> <del>POSITIVE</del> </div>
        </div>
    </div>
    <div class="column">
        <div class="ui negative message">
            <div class="content"> <del>NEGATIVE</del> </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">Icons #1</p>
    <div class="ui list">
        <div class="item">
            <i class="icon large poll"></i>
            <div class="content"> Umfragen (poll) </div>
        </div>
        <div class="item">
            <i class="icon large chartline"></i>
            <div class="content"> Dashboard (chartline) </div>
        </div>
        <div class="item">
            <i class="icon large chart pie"></i>
            <div class="content"> Reporting (chart pie)</div>
        </div>
        <div class="item">
            <i class="icon large chart bar"></i>
            <div class="content"> Statistik (chart bar)</div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">Icons #2</p>
    <div class="ui list">
        <div class="item">
            <i class="${Icons.PROVIDER} large"></i>
            <div class="content"> Anbieter (${Icons.PROVIDER})</div>
        </div>
        <div class="item">
            <i class="${Icons.VENDOR} large"></i>
            <div class="content"> Lieferanten (${Icons.VENDOR})</div>
        </div>
        <div class="item">
            <i class="${Icons.ORG} large"></i>
            <div class="content"> Einrichtungen (${Icons.ORG})</div>
        </div>
        <div class="item">
            <i class="${Icons.PLATFORM} large"></i>
            <div class="content"> Plattformen (${Icons.PLATFORM})</div>
        </div>
        <div class="item">
            <i class="${Icons.SUBSCRIPTION} large"></i>
            <div class="content"> Lizenzen (${Icons.SUBSCRIPTION})</div>
        </div>
        <div class="item">
            <i class="${Icons.LICENSE} large"></i>
            <div class="content"> Verträge (${Icons.LICENSE})</div>
        </div>
        <div class="item">
            <i class="${Icons.PACKAGE} large"></i>
            <div class="content"> Pakete (${Icons.PACKAGE})</div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">Icons #3</p>
    <div class="ui list">
        <div class="item">
            <i class="${Icons.SUBSCRIPTION_IS_MULTIYEAR} large"></i>
            <div class="content"> Mehrjahreslaufzeit (${Icons.SUBSCRIPTION_IS_MULTIYEAR})</div>
        </div>
        <div class="item">
            <i class="${Icons.COSTS} large"></i>
            <div class="content"> Kosten (${Icons.COSTS})</div>
        </div>
        <div class="item">
            <i class="${Icons.COSTS_CONFIG} large"></i>
            <div class="content"> Kosten (Konfiguration) (${Icons.COSTS_CONFIG})</div>
        </div>
        <div class="item">
            <i class="euro sign icon large"></i>
            <div class="content"> euro sign icon large </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">Links</p>
    <div class="ui list">
        <div class="item">
            <i class="icon large fake"></i>
            <div class="content"> internal link</div>
        </div>
        <div class="item">
            <i class="${Icons.LNK.FILTERED} large"></i>
            <div class="content"> internal link (redirect to list view with filter)</div>
        </div>
        <div class="item">
            <i class="${Icons.LNK.EXTERNAL} large"></i>
            <div class="content"> external link (${Icons.LNK.EXTERNAL})</div>
        </div>
        <div class="item">
            <i class="icon large fake"></i>
            <div class="content"> functional link</div>
        </div>
    </div>
</div>

<pre>
    springSecurity.errors.login.expired             ${message(code:'springSecurity.errors.login.expired')}
    springSecurity.errors.login.passwordExpired     ${message(code:'springSecurity.errors.login.passwordExpired')}
    springSecurity.errors.login.locked              ${message(code:'springSecurity.errors.login.locked')}
    springSecurity.errors.login.disabled            ${message(code:'springSecurity.errors.login.disabled')}
    springSecurity.errors.login.fail                ${message(code:'springSecurity.errors.login.fail')}
</pre>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large users"></i> roles
    </p>
    <div class="ui list">
        <div class="item">
            <div class="ui label">
                <i class="${Icons.AUTH.INST_USER}"></i>
                <g:message code="cv.roles.INST_USER"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="${Icons.AUTH.INST_EDITOR}"></i>
                <g:message code="cv.roles.INST_EDITOR"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="${Icons.AUTH.INST_ADM}"></i>
                <g:message code="cv.roles.INST_ADM"/>
            </div>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <div class="ui label yellow">
                <i class="${Icons.AUTH.ORG_INST_BASIC}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label yellow">
                <i class="${Icons.AUTH.ORG_INST_PRO}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')}
            </div>
        </div>

        <div class="item">
            <div class="ui label teal">
                <i class="${Icons.AUTH.ORG_CONSORTIUM_BASIC}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label teal">
                <i class="${Icons.AUTH.ORG_CONSORTIUM_PRO}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_PRO).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="icon certificate"></i>
                ?
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="icon tag"></i>
                ?
            </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large user lock"></i> user roles
    </p>
    <p>
        <g:set var="contextUser" value="${contextService.getUser()}" />
        <g:set var="contextOrg" value="${contextService.getOrg()}" />
<pre>
    contextUser: ${contextUser}
    contextOrg: ${contextOrg}

    md5: ${contextUser.id.encodeAsMD5()}

    SpringSecurityUtils.ifAnyGranted([])      : ${SpringSecurityUtils.ifAnyGranted([])}

    Roles                 : ${Role.executeQuery("select r from Role r where r.roleType not in ('org', 'fake') order by r.id").collect{ it.id + ':' + it.authority }}

    UserRoles             : ${UserRole.findAllByUser(contextUser)}

    contextUser.isYoda()  : ${contextUser.isYoda()}

    contextUser.isFormal(contextOrg)  : ${contextUser.isFormal(contextOrg)}
    contextUser.isComboInstAdminOf(contextOrg)  : ${contextUser.isComboInstAdminOf(contextOrg)}

    contextUser.isLastInstAdminOf(contextUser.formalOrg) : ${contextUser.isLastInstAdminOf(contextUser.formalOrg)}

    SpringSecurityUtils.ifAnyGranted('ROLE_YODA')  : ${SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}
    SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') : ${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}
    SpringSecurityUtils.ifAnyGranted('ROLE_USER')  : ${SpringSecurityUtils.ifAnyGranted('ROLE_USER')}

    contextUser.getAuthorities().authority.contains('ROLE_ADMIN') : ${contextUser.getAuthorities().authority.contains('ROLE_ADMIN')}
    contextUser.isAdmin() : ${contextUser.isAdmin()}

    contextUser.getAuthorities().authority.contains('ROLE_YODA') : ${contextUser.getAuthorities().authority.contains('ROLE_YODA')}
    contextUser.isYoda() : ${contextUser.isYoda()}
</pre>

</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large database"></i> granted permissions
    </p>
    <p>
    <pre>
    <g:each in="${Role.executeQuery('select r, pg, p from Role r join r.grantedPermissions pg join pg.perm p')}" var="e">
        ${e[0].authority} (${e[0].roleType}) - ${e[2].code}</g:each>
    </pre>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large kiwi bird"></i> simple color helper
    </p>
    <p>
        <i class="icon large stop red"></i> fomantic red <br/>
        <i class="icon large stop sc_red"></i> fallback <br/>

        <i class="icon large stop blue"></i> fomantic blue <br/>
        <i class="icon large stop sc_blue"></i> fallback <br/>

        <i class="icon large stop yellow"></i> fomantic yellow <br/>
        <i class="icon large stop sc_yellow"></i> fallback <br/>

        <i class="icon large stop green"></i> fomantic green <br/>
        <i class="icon large stop olive"></i> fomantic olive <br/>
        <i class="icon large stop sc_green"></i> fallback <br/>

        <i class="icon large stop orange"></i> fomantic orange <br/>
        <i class="icon large stop sc_orange"></i> fallback <br/>

        <i class="icon large stop sc_grey"></i> fallback <br/>

        <i class="icon large stop sc_darkgrey"></i> fallback <br/>
    </p>

    <p>
        <a class="ui primary label">Primary</a>
        <a class="ui secondary label">Secondary</a>
        <a class="ui red label">Red</a>
        <a class="ui orange label">Orange</a>
        <a class="ui yellow label">Yellow</a>
        <a class="ui olive label">Olive</a>
        <a class="ui green label">Green</a>
        <a class="ui teal label">Teal</a>
        <a class="ui blue label">Blue</a>
        <a class="ui violet label">Violet</a>
        <a class="ui purple label">Purple</a>
        <a class="ui pink label">Pink</a>
        <a class="ui brown label">Brown</a>
        <a class="ui grey label">Grey</a>
        <a class="ui black label">Black</a>
    </p>
</div>

<div class="ui segment">
</div>

<laser:htmlEnd />
