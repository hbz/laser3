<%@ page import="de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Various" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item active">Various</g:link>
    <g:link controller="dev" action="icons" class="item"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="buttons" class="item"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Buttons</g:link>
    <g:link controller="dev" action="markdown" class="item"><i class="${Icon.SIG.NEW_OBJECT} orange"></i> Markdown</g:link>
</nav>

<div class="ui four column grid">
    <div class="column">
        <div class="ui icon info message">
            <i class="${Icon.UI.INFO}"></i>
            <div class="content"> INFO </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon warning message">
            <i class="${Icon.UI.WARNING}"></i>
            <div class="content"> WARNING </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon success message">
            <i class="${Icon.UI.SUCCESS}"></i>
            <div class="content"> SUCCESS </div>
        </div>
    </div>
    <div class="column">
        <div class="ui icon error message">
            <i class="${Icon.UI.ERROR}"></i>
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
            <i class="${Icon.SURVEY} large"></i>
            <div class="content"> Umfragen (${Icon.SURVEY}) </div>
        </div>
        <div class="item">
            <i class="icon large chartline"></i>
            <div class="content"> Dashboard (chartline) </div>
        </div>
        <div class="item">
            <i class="${Icon.REPORTING} large"></i>
            <div class="content"> Reporting (${Icon.REPORTING})</div>
        </div>
        <div class="item">
            <i class="${Icon.STATS} large"></i>
            <div class="content"> Statistik (${Icon.STATS})</div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">Icons #3</p>
    <div class="ui list">
        <div class="item">
            <i class="${Icon.ATTR.SUBSCRIPTION_IS_MULTIYEAR} large"></i>
            <div class="content"> Mehrjahreslaufzeit (${Icon.ATTR.SUBSCRIPTION_IS_MULTIYEAR})</div>
        </div>
        <div class="item">
            <i class="${Icon.FNC.COST} large"></i>
            <div class="content"> Kosten (${Icon.FNC.COST})</div>
        </div>
        <div class="item">
            <i class="${Icon.FNC.COST_CONFIG} large"></i>
            <div class="content"> Kosten (Konfiguration) (${Icon.FNC.COST_CONFIG})</div>
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
            <i class="${Icon.LNK.FILTERED} large"></i>
            <div class="content"> internal link (redirect to list view with filter)</div>
        </div>
        <div class="item">
            <i class="${Icon.LNK.EXTERNAL} large"></i>
            <div class="content"> external link (${Icon.LNK.EXTERNAL})</div>
        </div>
        <div class="item">
            <i class="icon large fake"></i>
            <div class="content"> functional link</div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">[..]</p>

    <g:link controller="admin" action="systemEventsX" class="${Btn.SIMPLE}">
        <i class="shoe prints icon"></i> System Events X
    </g:link>

    <pre>
    springSecurity.errors.login.expired             ${message(code:'springSecurity.errors.login.expired')}
    springSecurity.errors.login.passwordExpired     ${message(code:'springSecurity.errors.login.passwordExpired')}
    springSecurity.errors.login.locked              ${message(code:'springSecurity.errors.login.locked')}
    springSecurity.errors.login.disabled            ${message(code:'springSecurity.errors.login.disabled')}
    springSecurity.errors.login.fail                ${message(code:'springSecurity.errors.login.fail')}
    </pre>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon users"></i> roles
    </p>
    <div class="ui list">
        <div class="item">
            <div class="ui label">
                <i class="${Icon.AUTH.INST_USER}"></i>
                <g:message code="cv.roles.INST_USER"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="${Icon.AUTH.INST_EDITOR}"></i>
                <g:message code="cv.roles.INST_EDITOR"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="${Icon.AUTH.INST_ADM}"></i>
                <g:message code="cv.roles.INST_ADM"/>
            </div>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <div class="ui label yellow">
                <i class="${Icon.AUTH.ORG_INST_BASIC}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label yellow">
                <i class="${Icon.AUTH.ORG_INST_PRO}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')}
            </div>
        </div>

        <div class="item">
            <div class="ui label teal">
                <i class="${Icon.AUTH.ORG_CONSORTIUM_BASIC}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label teal">
                <i class="${Icon.AUTH.ORG_CONSORTIUM_PRO}" style="color:#FFFFFF;"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_PRO).getI10n('authority')}
            </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon user lock"></i> user roles
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
        <icon:database /> granted permissions
    </p>
    <p>
    <pre>
    <g:each in="${Role.executeQuery('select r, pg, p from Role r join r.grantedPermissions pg join pg.perm p')}" var="e">
        ${e[0].authority} (${e[0].roleType}) - ${e[2].code}</g:each>
    </pre>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon kiwi bird"></i> simple color helper
    </p>
    <p>
        <i class="${Icon.SYM.SQUARE} large red"></i> fomantic red <br/>
        <i class="${Icon.SYM.SQUARE} large sc_red"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large blue"></i> fomantic blue <br/>
        <i class="${Icon.SYM.SQUARE} large sc_blue"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large yellow"></i> fomantic yellow <br/>
        <i class="${Icon.SYM.SQUARE} large sc_yellow"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large green"></i> fomantic green <br/>
        <i class="${Icon.SYM.SQUARE} large olive"></i> fomantic olive <br/>
        <i class="${Icon.SYM.SQUARE} large sc_green"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large orange"></i> fomantic orange <br/>
        <i class="${Icon.SYM.SQUARE} large sc_orange"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large sc_grey"></i> fallback <br/>

        <i class="${Icon.SYM.SQUARE} large sc_darkgrey"></i> fallback <br/>
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
