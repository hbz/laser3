<laser:htmlStart message="menu.yoda.appControllers" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.appControllers" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.appControllers" type="yoda" />

<h2 class="ui header">Roles and Hierarchies</h2>

    <p><strong>Global User Roles</strong></p>

    <div class="ui list secInfoWrapper">
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> &rArr;
            <span class="ROLE_ADMIN">ROLE_ADMIN</span> &rArr;
            <span class="ROLE_USER">ROLE_USER</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">IS_AUTHENTICATED_FULLY</span>
        </div>
    </div>

    <p><strong>Org Roles (Customer Types)</strong></p>

    <div class="ui list secInfoWrapper">
        <div class="item">
            <span class="IS_AUTHENTICATED_FULLY">ORG_INST_PRO</span> &rArr;
            <span class="ROLE_USER">ORG_INST_BASIC</span> |
            <span class="ROLE_DATAMANAGER">ORG_CONSORTIUM_PRO</span> &rArr;
            <span class="ROLE_DATAMANAGER">ORG_CONSORTIUM_BASIC</span>
        </div>
    </div>

    <p><strong>User Org Roles</strong></p>

    <div class="ui list secInfoWrapper">
        <div class="item">
            <span>INST_ADM</span> &rArr;
            <span>INST_EDITOR</span> &rArr;
            <span>INST_USER</span>  &nbsp; (implizite Prüfung auf <span class="ROLE_USER">ROLE_USER</span>)
        </div>
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> und <span class="ROLE_ADMIN">ROLE_ADMIN</span> liefern <code>TRUE</code>
        </div>
    </div>

<h2 class="ui header">App Controllers</h2>

<g:each in="${controller}" var="c">
    <a href="#jumpMark_${c.key}">
        <g:if test="${c.value.deprecated}"><em>${c.key.replace('Controller', '')} *</em></g:if>
        <g:else>${c.key.replace('Controller', '')}</g:else>
    </a>
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
                    <g:if test="${c.value.deprecated}"><em>&larr; Deprecated</em></g:if>
                    <g:each in="${c.value.secured}" var="cSecured">
                        <span class="${cSecured}">${cSecured}</span> &nbsp;
                    </g:each>
                </h3>

                <div class="ui segment">
                    <div class="ui relaxed divided list">
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
                                            <g:set var="infoValue" value="${Arrays.toString(ss).replace('[','').replace(']','')}" />
                                            <span class="${infoValue}">${infoValue}</span>
                                        </g:each>
                                    </g:elseif>
                                    <g:elseif test="${info.key == 'check404'}">
                                        <strong class="warning">[404]</strong>
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
                        <div class="ui relaxed divided list">
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
                                                <g:set var="infoValue" value="${Arrays.toString(ss).replace('[','').replace(']','')}" />
                                                <span class="${infoValue}">${infoValue}</span>
                                            </g:each>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'check404'}">
                                            <strong class="warning">[404]</strong>
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
                                        <g:elseif test="${info.key == 'modifiers'}">
                                            <g:if test="${info.value.private == true}">
                                                <strong class="modifier">private</strong>
                                            </g:if>
                                            <g:if test="${info.value.static == true}">
                                                <strong class="modifier">static</strong>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${info.key == 'deprecated'}">
                                            <em>&larr; Deprecated</em>
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
    padding: 0 5px;
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
    padding: 1px 3px;
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
    margin-left: 0.5em;
    padding: 1px 3px;
    min-width: 90px;
    text-align: center;
    font-weight: normal;
    background-color: #eee;
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
</style>

<laser:htmlEnd />

