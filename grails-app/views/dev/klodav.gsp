<%@ page import="de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground" serviceInjection="true" />

<br />
<br />

<h1 class="ui header center aligned">
    Playground
</h1>


<div class="ui segment">
    <p class="ui header">
        <i class="icon large users"></i> roles
    </p>
    <div class="ui list">
        <div class="item">
            <i class="icon large user   inverted teal"></i>
            <div class="content"> <g:message code="cv.roles.INST_USER"/> </div>
        </div>
        <div class="item">
            <i class="icon large user edit   inverted teal"></i>
            <div class="content"> <g:message code="cv.roles.INST_EDITOR"/> </div>
        </div>
        <div class="item">
            <i class="icon large user cog   inverted teal"></i>
            <div class="content"> <g:message code="cv.roles.INST_ADM"/> </div>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <i class="icon large star half outline"></i>
            <div class="content"> ${Role.findByAuthority(CustomerTypeService.ORG_INST_BASIC).getI10n('authority')} </div>
        </div>
        <div class="item">
            <i class="icon large star outline"></i>
            <div class="content"> ${Role.findByAuthority(CustomerTypeService.ORG_INST_PRO).getI10n('authority')} </div>
        </div>
        <div class="item">
            <i class="icon large tag"></i>
            <div class="content"> ? </div>
        </div>
        <div class="item">
            <i class="icon large star half"></i>
            <div class="content"> ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_BASIC).getI10n('authority')} </div>
        </div>
        <div class="item">
            <i class="icon large star"></i>
            <div class="content"> ${Role.findByAuthority(CustomerTypeService.ORG_CONSORTIUM_PRO).getI10n('authority')} </div>
        </div>
        <div class="item">
            <i class="icon large certificate"></i>
            <div class="content"> ? </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large user lock"></i> user roles
    </p>
    <p>
    <pre>
        Roles                 : ${Role.executeQuery("select r from Role r where r.roleType not in ('org', 'fake') order by r.id").collect{ it.id + ':' + it.authority }}

        UserRoles             : ${UserRole.findAllByUser(contextService.getUser())}

        UserOrgRoles          : ${UserOrgRole.findAllByUser(contextService.getUser()).collect{ '(' + it.user.id + ',' + it.org.id + ',' + it.formalRole.id + ')'}}

        contextService.getUser().isYoda()  : ${contextService.getUser().isYoda()}

        User.get(77).isYoda()  : ${User.get(77).isYoda()}
        User.get(88).isYoda()  : ${User.get(88).isYoda()}
        User.get(99).isYoda()  : ${User.get(99).isYoda()}

        SpringSecurityUtils.ifAnyGranted('ROLE_YODA')  : ${SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}
        SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') : ${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}
        SpringSecurityUtils.ifAnyGranted('ROLE_USER')  : ${SpringSecurityUtils.ifAnyGranted('ROLE_USER')}
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
</div>

<div class="ui segment">
</div>

<laser:htmlEnd />
