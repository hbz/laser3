<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appSecurity')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.appSecurity" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.appSecurity')}</h1>

<h2 class="ui header">Hierarchical Global Roles</h2>

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

<h2 class="ui header">Independent Global Roles</h2>

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

<h2 class="ui header">Hierarchical Org Roles (Customer Types)</h2>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="IS_AUTHENTICATED_FULLY">ORG_INST</span> &rArr;
            <span class="ROLE_USER">ORG_BASIC_MEMBER</span> |
            <span class="ROLE_DATAMANAGER">ORG_CONSORTIUM</span> |
            <span class="ROLE_API">FAKE</span>
        </div>
    </div>
</div>

<h2 class="ui header">Hierarchical User Roles</h2>

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

<h2 class="ui header">Controller Security</h2>

<g:each in="${controller}" var="c">
    <a href="#jumpMark_${c.key}">${c.key.replace('Controller', '')}</a>
    &nbsp;&nbsp;
</g:each>

<br />
<br />

<button id="resultToggle" class="ui button">Hier klicken zum Ändern der Ansicht</button>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.resultViewModes = [
        'Sichtbar: Alle Methoden',
        'ToDo: Nicht transaktionsgesicherte Methoden',
        'Sichtbar: Überarbeitete Methoden',
    ]
    JSPC.app.resultViewMode = 0

    $('#resultToggle').on('click', function(){

        if (++JSPC.app.resultViewMode >= JSPC.app.resultViewModes.length) {
            JSPC.app.resultViewMode = 0
        }
        $('#resultToggle').html(JSPC.app.resultViewModes[JSPC.app.resultViewMode])

        if (JSPC.app.resultViewMode == 0) {
            $('.secInfoWrapper2 .list .item').removeClass('hidden')
        }
        else if (JSPC.app.resultViewMode == 1) {
            $('.secInfoWrapper2 .list .item.refactoring-done').addClass('hidden')
            $('.secInfoWrapper2 .list .item:not(.refactoring-done)').removeClass('hidden')

        }
        else if (JSPC.app.resultViewMode == 2) {
            $('.secInfoWrapper2 .list .item:not(.refactoring-done)').addClass('hidden')
            $('.secInfoWrapper2 .list .item.refactoring-done').removeClass('hidden')
        }
    })
</laser:script>

<br />
<br />

<div class="ui grid">
    <div class="sixteen wide column">

        <div class="secInfoWrapper secInfoWrapper2">
            <g:each in="${controller}" var="c">

                <h3 class="ui header" id="jumpMark_${c.key}">
                    ${c.key}
                    <g:each in="${c.value.secured}" var="cSecured">
                        <span class="${cSecured}">${cSecured}</span> &nbsp;
                    </g:each>
                </h3>

                <div class="ui segment">
                    <div class="ui divided list">
                        <g:each in="${c.value.methods.public}" var="method">
                            <g:set var="refactoringDone" value="${method.value?.refactoring == 'done'}" />
                            <div class="item${refactoringDone ? ' refactoring-done':''}">
                                <g:link controller="${c.key.split('Controller')[0]}" action="${method.key}">${method.key}</g:link>

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
                                        <strong class="${info.value}">@${info.value}</strong>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'ctrlService'}">
                                        <strong class="${info.key}_${info.value}">ctrlService</strong>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'wtc'}">
                                        <strong class="${info.key}_${info.value}">withTransaction{}</strong>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'deprecated'}">
                                        <em>&larr; Deprecated</em>
                                    </g:elseif>

                                </g:each>
                            </div>
                        </g:each>
                    </div>
                </div>
                <g:if test="${c.value.methods.others}">
                    <div class="ui segment">
                        <div class="ui divided list">
                            <g:each in="${c.value.methods.others}" var="method">
                                <div class="item">
                                    <g:link controller="${c.key.split('Controller')[0]}" action="${method.key}">${method.key}</g:link>

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
                                            <strong class="${info.value}">@${info.value}</strong>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'ctrlService'}">
                                            <strong class="${info.key}_${info.value}">ctrlService</strong>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'wtc'}">
                                            <strong class="${info.key}_${info.value}">withTransaction{}</strong>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'deprecated'}">
                                            <em>&larr; Deprecated</em>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'modifiers'}">
                                            <g:if test="${info.value.private == true}">
                                                <strong class="modifier">private</strong>
                                            </g:if>
                                            <g:if test="${info.value.static == true}">
                                                <strong class="modifier">static</strong>
                                            </g:if>
                                        </g:elseif>

                                    </g:each>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </g:if>
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
.secInfoWrapper .warning,
.secInfoWrapper .modifier,
.secInfoWrapper .wtc_1,
.secInfoWrapper .ctrlService_1,
.secInfoWrapper .wtc_2,
.secInfoWrapper .ctrlService_2 {
    margin-right: 1em;
    padding: 1px 5px;
    min-width: 90px;
    text-align: center;
    font-weight: normal;
    background-color: #eee;
    float: left;
}

.secInfoWrapper .warning {
    color: orangered;
}
.secInfoWrapper .modifier {
    color: slategrey;
}

.secInfoWrapper .wtc_1,
.secInfoWrapper .ctrlService_1 {
    color: green;
    opacity: 0.4;
}

.secInfoWrapper .transactional,
.secInfoWrapper .wtc_2,
.secInfoWrapper .ctrlService_2 {
    color: green;
}

.secInfoWrapper .affil,
.secInfoWrapper .perm,
.secInfoWrapper .type,
.secInfoWrapper .specRole {
    font-size: 90%;
    color: #335555;
}
.secInfoWrapper .perm {
    font-weight: normal;
}
.secInfoWrapper .type {
    font-style: italic;
}
.secInfoWrapper .test {
    color: #dd33dd;
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

