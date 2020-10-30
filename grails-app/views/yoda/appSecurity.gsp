<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.security')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.security" class="active"/>
</semui:breadcrumbs>
<br />
<h2 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.security')}</h2>

<h3 class="ui header">Hierarchical Global Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> &rArr;
            <span class="ROLE_ADMIN">ROLE_ADMIN</span> &rArr;
            <span class="ROLE_USER">ROLE_USER</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">IS_AUTHENTICATED_FULLY</span>
        </div>
    </div>
</div>

<h3 class="ui header">Independent Global Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_GLOBAL_DATA">ROLE_GLOBAL_DATA</span> |
            <span class="ROLE_ORG_EDITOR">ROLE_ORG_EDITOR</span> |
            <span class="ROLE_PACKAGE_EDITOR">ROLE_PACKAGE_EDITOR</span> |
            <span class="ROLE_STATISTICS_EDITOR">ROLE_STATISTICS_EDITOR</span> |
            <span class="ROLE_TICKET_EDITOR">ROLE_TICKET_EDITOR</span> |
            <span class="ROLE_API">ROLE_API</span>
        </div>
    </div>
</div>

<h3 class="ui header">Hierarchical Org Roles (Customer Types)</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_YODA">ORG_INST_COLLECTIVE</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">ORG_INST</span> &rArr;
            <span class="ROLE_USER">ORG_BASIC_MEMBER</span> |
            <span class="ROLE_DATAMANAGER">ORG_CONSORTIUM</span> |
            <span class="ROLE_API">FAKE</span>
        </div>
    </div>
</div>

<h3 class="ui header">Hierarchical User Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span>INST_ADM</span> &rArr;
            <span>INST_EDITOR</span> &rArr;
            <span>INST_USER</span>  &nbsp; (implizite Prüfung auf <span class="ROLE_USER">ROLE_USER</span>)
        </div>
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> und <span class="ROLE_ADMIN">ROLE_ADMIN</span> liefern <code>TRUE</code>
        </div>
    </div>
</div>

<h3 class="ui header">Controller Security</h3>

<g:each in="${controller}" var="c">
    <a href="#jumpMark_${c.key}">${c.key}</a>
    &nbsp;&nbsp;
</g:each>

<br />
<br />

<div class="ui grid">
    <div class="sixteen wide column">

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
                                <g:link controller="${c.key.split('Controller')[0]}" action="${method.key}">${method.key}</g:link>

                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

                                <g:each in="${method.value}" var="info">

                                    <g:if test="${info.key == 'warning'}">
                                        <strong class="${info.key}">${info.value}</strong>
                                    </g:if>
                                    <g:elseif test="${info.key == 'debug'}">
                                        <g:each in="${info.value}" var="dd">
                                            <g:if test="${dd.value}">
                                                <span class="${dd.key}">${dd.value}</span>
                                            </g:if>
                                        </g:each>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'secured'}">
                                        <g:each in="${info.value}" var="ss">
                                            <span class="${ss.value}">${ss.value}</span>
                                        </g:each>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'transactional'}">
                                        <strong class="${info.value}">${info.value}</strong>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'ctrl'}">
                                        <strong class="ctrl_${info.value}">ctrl: ${info.value}</strong>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'deprecated'}">
                                        <em>Deprecated</em>
                                    </g:elseif>

                                </g:each>
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:each>
        </div>

    </div>
</div>

<style>
.secInfoWrapper span {
    font-weight: bolder;
    padding: 0px 5px;
    color: #FF00FF;
}
.secInfoWrapper2 span {
    margin-left: 2px;
    float: right;
}
.secInfoWrapper .list .item:hover {
    background-color: #f5f5f5;
}
.secInfoWrapper .permitAll {
    padding: 1px 5px;
    color: #fff;
    background-color: #ff0066;
}

.secInfoWrapper .transactional,
.secInfoWrapper .ctrl_2 {
    padding: 1px 5px;
    color: green;
    background-color: #eee;
}
.secInfoWrapper .warning ,
.secInfoWrapper .ctrl_1 {
    padding: 1px 5px;
    color: black;
    background-color: #eee;
}
.secInfoWrapper .IS_AUTHENTICATED_FULLY {
    color: #228B22;
}
.secInfoWrapper .ROLE_YODA {
    color: crimson;
}
.secInfoWrapper .ROLE_ADMIN {
    color: orange;
}
.secInfoWrapper .ROLE_DATAMANAGER {
    color: #8B008B;
}
.secInfoWrapper .ROLE_USER {
    color: #1E90FF;
}
.secInfoWrapper .ROLE_API,
.secInfoWrapper .ROLE_API_READER,
.secInfoWrapper .ROLE_API_WRITER,
.secInfoWrapper .ROLE_API_DATAMANAGER {
    color: #87CEEB;
    font-style: italic;
}
.secInfoWrapper .ROLE_ORG_EDITOR,
.secInfoWrapper .ROLE_PACKAGE_EDITOR,
.secInfoWrapper .ROLE_STATISTICS_EDITOR,
.secInfoWrapper .ROLE_TICKET_EDITOR {
    color: #8B4513;
}
</style>
</body>
</html>

