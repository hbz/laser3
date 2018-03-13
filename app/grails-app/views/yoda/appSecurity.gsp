<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} Application Security</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Application Security" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"><semui:headerIcon />Application Security</h1>

<h3 class="ui header">Global Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> &rArr;
            <span class="ROLE_ADMIN">ROLE_ADMIN</span> &rArr;
            <span class="ROLE_DATAMANAGER">ROLE_DATAMANAGER</span> &rArr;
            <span class="ROLE_USER">ROLE_USER</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">IS_AUTHENTICATED_FULLY</span>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <span class="ROLE_PACKAGE_EDITOR">ROLE_PACKAGE_EDITOR</span> |
            <span class="ROLE_ORG_EDITOR">ROLE_ORG_EDITOR</span>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <span class="ROLE_API">ROLE_API</span> |
            <span class="ROLE_API_READER">ROLE_API_READER</span> |
            <span class="ROLE_API_WRITER">ROLE_API_WRITER</span>
        </div>
    </div>
</div>

<h3 class="ui header">Current User Roles ; checked with sec:ifAnyGranted</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <g:each in="${com.k_int.kbplus.auth.Role.findAll()}" var="role">
            <sec:ifAnyGranted roles="${role.authority}"><div class="item">${role.authority} (${role.roleType})</div></sec:ifAnyGranted>
        </g:each>
    </div>
</div>


<br />

    <div class="ui grid">
        <div class="twelve wide column">

            <div class="secInfoWrapper secInfoWrapper2">
                <g:each in="${controller}" var="c">

                    <h5 class="ui header" id="jumpMark_${c.key}">
                        ${c.key}
                        <g:each in="${c.value.secured}" var="cSecured">
                            <span class="${cSecured}">${cSecured}</span> &nbsp;
                        </g:each>
                    </h5>

                    <div class="ui segment">
                        <div class="ui divided list">
                            <g:each in="${c.value.methods}" var="method">
                                <div class="item">
                                    ${method.key}
                                    <g:each in="${method.value}" var="v">
                                        <span class="${v}">${v}</span>
                                    </g:each>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </g:each>
            </div>

        </div>
        <div class="four wide column">
            <div class="ui sticky">
                <aside>
                    <g:each in="${controller}" var="c">
                        <a href="#jumpMark_${c.key}">${c.key.replaceFirst('com.k_int.kbplus.','').replaceAll('Controller', '  ')}</a> |
                    </g:each>
                    <a href="#jumpMark_top">&uArr;</a>
                </aside>
            </div>
        </div>
    </div>

</body>
</html>
