<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground" serviceInjection="true" />

<br />
<br />

<h1 class="ui header center aligned">
    Playground
</h1>

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
            <i class="icon large boxes"></i>
            <div class="content"> Lieferanten (boxes)</div>
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
            <i class="${Icons.SUB_IS_MULTIYEAR} large"></i>
            <div class="content"> Mehrjahreslaufzeit (${Icons.SUB_IS_MULTIYEAR})</div>
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
            <i class="${Icons.LINK_FILTERED} large"></i>
            <div class="content"> internal link (redirect to list view with filter)</div>
        </div>
        <div class="item">
            <i class="${Icons.LINK_EXTERNAL} large"></i>
            <div class="content"> external link (${Icons.LINK_EXTERNAL})</div>
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
                <i class="icon user"></i>
                <g:message code="cv.roles.INST_USER"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="icon user edit"></i>
                <g:message code="cv.roles.INST_EDITOR"/>
            </div>
        </div>
        <div class="item">
            <div class="ui label">
                <i class="icon user shield"></i>
                <g:message code="cv.roles.INST_ADM"/>
            </div>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <div class="ui label yellow">
                <i class="icon user circle"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label yellow">
                <i class="icon trophy"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')}
            </div>
        </div>

        <div class="item">
            <div class="ui label teal">
                <i class="icon user circle"></i>
                ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')}
            </div>
        </div>
        <div class="item">
            <div class="ui label teal">
                <i class="icon trophy"></i>
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
