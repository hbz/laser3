<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')}</title>
</head>
<body>

    <div>
        <ul class="breadcrumb">
            <li> <g:link controller="home" action="index">Home</g:link> <span class="divider">/</span> </li>
            <li> <g:link controller="licenseDetails" action="index" id="${params.id}">ONIX-PL ${message(code:'license.details')}</g:link> </li>
        </ul>
    </div>

    <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
    </g:if>

    <h1 class="ui header">ONIX-PL License : ${onixplLicense?.title}</h1>

<div>
    <div class="row">
        <div class="span8">

            <h6 class="ui header">${message(code:'laser', default:'LAS:eR')} ${message(code:'license.information')}</h6>

            <g:if test="${!onixplLicense}">
            ${message(code:'onix.cannot.find.license')}
            </g:if>
            <g:else>
            <div class="inline-lists">
                <dl>
                    <dt><label class="control-label" for="license">Reference</label></dt>
                    <dd>
                        <g:each in="${onixplLicense.licenses}">
                            <g:link name="license" controller="licenseDetails" action="index" id="${it.id}">${it.reference}</g:link>
                        </g:each>
                    </dd>
                </dl>
                </div>

            <h6 class="ui header">ONIX-PL License Properties</h6>

            
            </g:else>
        </div>
    </div>
</div>

</body>
</html>