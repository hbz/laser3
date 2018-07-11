<%@ page import="com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body>

    <h1>Here we go ..</h1>

        <g:each in="${subscriptions}" var="sub" status="i">
            <br />
            <br />
            <!-- subscription -->

            &nbsp; SUB: ${i} - ${sub}

            <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))}" var="role">
                <br />
                &nbsp;&nbsp;&nbsp; Anbieter: ${role.org?.name}
            </g:each>

            <br />
            &nbsp;&nbsp;&nbsp; Lizenztyp: ${sub.type?.getI10n('value')}

            <br />
            &nbsp;&nbsp;&nbsp; Konsortium: ${sub.getConsortia()?.name}

            <g:each in="${sub.packages}" var="subPkg" status="j">
                <br />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; PKG: ${j} - ${subPkg.pkg}
            </g:each>

            <!-- subscription -->
        </g:each>

</body>