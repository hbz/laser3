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

<div class="secInfoWrapper">

    <span class="ROLE_YODA">ROLE_YODA</span> >
    <span class="ROLE_ADMIN">ROLE_ADMIN</span> >
    <span class="ROLE_DATAMANAGER">ROLE_DATAMANAGER</span> >
    <span class="ROLE_USER">ROLE_USER</span> >
    <span class="IS_AUTHENTICATED_FULLY">IS_AUTHENTICATED_FULLY</span>

    <br /><br />

    <span class="ROLE_PACKAGE_EDITOR">ROLE_PACKAGE_EDITOR</span> <br />
    <span class="ROLE_ORG_EDITOR">ROLE_ORG_EDITOR</span>

    <br /><br />

    <span class="ROLE_API">ROLE_API</span> <br />
    <span class="ROLE_API_READER">ROLE_API_READER</span> <br />
    <span class="ROLE_API_WRITER">ROLE_API_WRITER</span>

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
