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

<h1 class="ui header">Application Security</h1>

<pre>
    ROLE_YODA > ROLE_ADMIN > ROLE_DATAMANAGER > ROLE_USER

    ROLE_PACKAGE_EDITOR
    ROLE_ORG_EDITOR

    ROLE_API
    ROLE_API_READER
    ROLE_API_WRITER
</pre>

    <div class="secInfoWrapper">
        <g:each in="${controller}" var="c">
            <h5 class="ui header">${c.key}</h5>
                <div class="ui bulleted list">
                    <g:each in="${c.value}" var="m">
                        <div class="item">
                            ${m.key}
                            <g:each in="${m.value}" var="v">
                                <span class="${v}">${v}</span>
                            </g:each>
                        </div>
                    </g:each>
                </div>
            </h6>
        </g:each>
    </div>

</body>
</html>
