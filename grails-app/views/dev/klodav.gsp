<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.auth.Role" %>
<laser:htmlStart text="Frontend for Developers" serviceInjection="true" />

<br />
<br />

<h1 class="ui header center aligned">
    Reminder &amp; Playground
</h1>



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
    <p class="ui header">
        <i class="icon large database"></i> user roles
    </p>
    <p>
    <pre>
        Roles                 : ${de.laser.auth.Role.executeQuery("select r from Role r where r.roleType not in ('org', 'fake') order by r.id").collect{ it.id + ':' + it.authority }}

        UserRoles             : ${de.laser.auth.UserRole.findAllByUser(contextService.getUser())}

        UserOrgRoles          : ${de.laser.auth.UserOrgRole.findAllByUser(contextService.getUser()).collect{ '(' + it.user.id + ',' + it.org.id + ',' + it.formalRole.id + ')'}}

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
    <g:each in="${de.laser.auth.Role.executeQuery('select r, pg, p from Role r join r.grantedPermissions pg join pg.perm p')}" var="e">
        ${e[0].authority} (${e[0].roleType}) - ${e[2].code}</g:each>
    </pre>
</div>

<div class="ui segment">
</div>

<laser:htmlEnd />
