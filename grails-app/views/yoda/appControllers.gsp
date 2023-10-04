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
            <span class="ROLE_USER">ORG_INST_PRO</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">ORG_INST_BASIC</span> |
            <span class="ROLE_YODA">ORG_SUPPORT</span> &rArr;
            <span class="ROLE_USER">ORG_CONSORTIUM_PRO</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">ORG_CONSORTIUM_BASIC</span>
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

%{--<button id="resultToggle" class="ui button">Hier klicken zum Ändern der Ansicht</button>--}%

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
            $('.secInfoWrapper2 .grid .row').removeClass('hidden')
        }
        else if (JSPC.app.resultViewMode == 1) {
            $('.secInfoWrapper2 .grid .row.refactoring-done').addClass('hidden')
            $('.secInfoWrapper2 .grid .row:not(.refactoring-done)').removeClass('hidden')

        }
        else if (JSPC.app.resultViewMode == 2) {
            $('.secInfoWrapper2 .grid .row:not(.refactoring-done)').addClass('hidden')
            $('.secInfoWrapper2 .grid .row.refactoring-done').removeClass('hidden')
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
                    <div class="ui vertically divided grid">
                        <g:each in="${c.value.methods.public}" var="method">
                            <g:set var="refactoringDone" value="${method.value?.refactoring == 'done'}" />
                            <div class="row${refactoringDone ? ' refactoring-done':''}">
                                <div class="five wide column">
                                    <g:link controller="${c.key.split('Controller')[0]}" action="${method.key}">${method.key}</g:link>

                                    <g:each in="${method.value}" var="info">
                                        <g:if test="${info.key == 'modifiers'}">
                                            <g:if test="${info.value.private == true}">
                                                <strong class="modifier">private</strong>
                                            </g:if>
                                            <g:if test="${info.value.static == true}">
                                                <strong class="modifier">static</strong>
                                            </g:if>
                                        </g:if>
                                        <g:elseif test="${info.key == 'deprecated'}">
                                            <em class="deprecated">@deprecated</em>
                                        </g:elseif>
                                    </g:each>
                                </div>
                                <div class="three wide column">
                                    <g:each in="${method.value}" var="info">
                                        <g:if test="${info.key == 'warning'}">
                                            <strong class="${info.key}">${info.value}</strong>
                                        </g:if>
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
                                    </g:each>
                                </div>
                                <div class="eight wide column">
                                    <g:each in="${method.value}" var="info">
                                        <g:if test="${info.key == 'debug'}">
                                            <g:each in="${info.value}" var="dd">
                                                <g:if test="${dd.value}">
                                                    <span class="${dd.key}">
                                                        <g:if test="${dd.key == 'perm'}">
                                                            ${dd.value.replaceAll('ORG_', '')}
                                                        </g:if>
                                                        <g:elseif test="${dd.key == 'test'}">
                                                            <g:if test="${dd.value.contains('_denySupport_')}">
                                                                [ - ] closure_denySupport
                                                            </g:if>
                                                            <g:else>
                                                                [ + ] closure
                                                            </g:else>
                                                        </g:elseif>
                                                        <g:else>
                                                            ${dd.value}
                                                        </g:else>
                                                    </span>

                                                    <g:if test="${dd.key == 'perm'}">
                                                        <g:if test="${dd.value.contains('ORG_INST_BASIC')}">
                                                            <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_INST_BASIC / ORG_INST_PRO">
                                                                <i class="icon user circle yellow"></i><i class="icon trophy yellow"></i>
                                                            </span>
                                                        </g:if>
                                                        <g:if test="${dd.value.contains('ORG_INST_PRO')}">
                                                            <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_INST_PRO">
                                                                <i class="icon trophy yellow"></i>
                                                            </span>
                                                        </g:if>
                                                        <g:if test="${dd.value.contains('ORG_CONSORTIUM_BASIC')}">
                                                            <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_CONSORTIUM_BASIC / ORG_CONSORTIUM_PRO">
                                                                <i class="icon user circle teal"></i><i class="icon trophy teal"></i>
                                                            </span>
                                                        </g:if>
                                                        <g:if test="${dd.value.contains('ORG_CONSORTIUM_PRO')}">
                                                            <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_CONSORTIUM_PRO">
                                                                <i class="icon trophy teal"></i>
                                                            </span>
                                                        </g:if>
                                                    </g:if>
                                                    <g:if test="${dd.key == 'test'}">
                                                        <g:if test="${info.value.getAt('perm')?.contains('ORG_CONSORTIUM_BASIC') || info.value.getAt('perm')?.contains('ORG_CONSORTIUM_PRO') || info.value.getAt('perm')?.contains('ORG_SUPPORT')}">%{-- check with given perms --}%
                                                            <g:if test="${! dd.value.contains('_denySupport_')}">
                                                                <i class="icon theater masks red"></i>
                                                            </g:if>
                                                        </g:if>
                                                    </g:if>

                                                </g:if>
                                            </g:each>

                                            <g:if test="${! info.value.getAt('perm')}">%{-- check without explicit perms --}%
                                                <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_INST_BASIC / ORG_INST_PRO">
                                                    <i class="icon user circle yellow"></i><i class="icon trophy yellow"></i>
                                                </span>
                                                <span class="la-long-tooltip" data-tooltip="Zugriff: ORG_CONSORTIUM_BASIC / ORG_CONSORTIUM_PRO">
                                                    <i class="icon user circle teal"></i><i class="icon trophy teal"></i>
                                                </span>
                                                <g:if test="${! info.value.getAt('test').contains('_denySupport_')}">
                                                    <i class="icon theater masks red"></i>
                                                </g:if>
                                            </g:if>
                                        </g:if>
                                        <g:elseif test="${info.key == 'secured'}">
                                            <g:each in="${info.value}" var="ss">
                                                <g:set var="infoValue" value="${Arrays.toString(ss).replace('[','').replace(']','')}" />
                                                <span class="${infoValue}">${infoValue}</span>
                                            </g:each>
                                        </g:elseif>
                                    </g:each>
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
                <g:if test="${c.value.methods.others}">
                    <div class="ui segment">
                        <div class="ui vertically divided grid">
                            <g:each in="${c.value.methods.others}" var="method">
                                <div class="row">
                                    <div class="five wide column">
                                        <g:link controller="${c.key.split('Controller')[0]}" action="${method.key}">${method.key}</g:link>

                                        <g:each in="${method.value}" var="info">
                                            <g:if test="${info.key == 'modifiers'}">
                                                <g:if test="${info.value.private == true}">
                                                    <strong class="modifier">private</strong>
                                                </g:if>
                                                <g:if test="${info.value.static == true}">
                                                    <strong class="modifier">static</strong>
                                                </g:if>
                                            </g:if>
                                            <g:elseif test="${info.key == 'deprecated'}">
                                                <em class="deprecated">@deprecated</em>
                                            </g:elseif>
                                        </g:each>
                                    </div>
                                    <div class="three wide column">
                                        <g:each in="${method.value}" var="info">
                                            <g:if test="${info.key == 'warning'}">
                                                <strong class="${info.key}">${info.value}</strong>
                                            </g:if>
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
                                        </g:each>
                                    </div>
                                    <div class="eight wide column">
                                        <g:each in="${method.value}" var="info">
                                            <g:if test="${info.key == 'debug'}">
                                                <g:each in="${info.value}" var="dd">
                                                    <g:if test="${dd.value}">
                                                        aa <span class="${dd.key}">${dd.value}</span>
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                            <g:elseif test="${info.key == 'secured'}">
                                                <g:each in="${info.value}" var="ss">
                                                    <g:set var="infoValue" value="${Arrays.toString(ss).replace('[','').replace(']','')}" />
                                                    <span class="${infoValue}">${infoValue}</span>
                                                </g:each>
                                            </g:elseif>
                                        </g:each>
                                    </div>
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
.secInfoWrapper2 span:not(.la-long-tooltip) {
    margin-left: 2px;
    float: right;
}
.secInfoWrapper .grid .row          { padding: 0; }
.secInfoWrapper .grid .row:hover    { background-color: #f5f5f5; }
.secInfoWrapper .grid .row .column  { margin-top: 0.5rem !important; margin-bottom: 0.5rem !important; }

.secInfoWrapper .permitAll {
    padding: 0.2rem 0.4rem;
    color: white;
    background-color: orangered;
}

.secInfoWrapper .transactional,
.secInfoWrapper .warning,
.secInfoWrapper .modifier,
.secInfoWrapper .wtc_1,
.secInfoWrapper .ctrlService_1,
.secInfoWrapper .wtc_2,
.secInfoWrapper .ctrlService_2 {
    margin-left: 0.5em;
    padding: 0.2rem 0.4rem;
    min-width: 90px;
    font-weight: normal;
    text-align: center;
    background-color: #eee;
}

.secInfoWrapper .warning {
    color: orangered;
}
.secInfoWrapper .modifier {
    color: slategrey;
}
.secInfoWrapper .deprecated {
    color: red;
    background-color: rgba(256,0,0,0.1);
    padding: 0.2rem 0.4rem;
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

.secInfoWrapper .perm,
.secInfoWrapper .type,
.secInfoWrapper .specRole {
    color: #335555;
}
.secInfoWrapper .affil {
    color: #ff55ff;
}
.secInfoWrapper .perm {
    font-weight: normal;
}
.secInfoWrapper .type {
    font-style: italic;
}
.secInfoWrapper .test {
    color: #dd33dd;
    text-decoration: underline dotted;
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

