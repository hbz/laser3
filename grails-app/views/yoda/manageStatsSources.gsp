<%@page import="grails.converters.JSON" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : Manage Stats Sources</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
            <semui:crumb text="Stats Sources" class="active" />
        </semui:breadcrumbs>

        <semui:messages data="${flash}" />

        <table class="ui celled la-table table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Provider</th>
                    <th>Platform</th>
                    <th>Base URL</th>
                    <th>Arguments</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${c4sources}" var="source">
                    <tr>
                        <g:form action="editStatsSource" name="editStatsSource4" params="[c4source: source.id]">
                            <td>Counter 4 - ${source.id}</td>
                            <td>${source.provider.name}</td>
                            <td>${source.platform.name}</td>
                            <td>${source.baseUrl}</td>
                            <td>
                                <fieldset id="c4${source.id}">
                                    <g:if test="${source.arguments}">
                                        <g:set var="args" value="${JSON.parse(source.arguments)}"/>
                                        <g:each in="${args}" var="arg" status="i">
                                            <div class="ui input arg" id="c4${source.id}arg${i}">
                                                <input type="text" name="arg[]" value="${arg.getKey()}" placeholder="argument"/>
                                                <input type="text" name="val[]" value="${arg.value}" placeholder="value"/>
                                                <button class="ui small button negative remArg" data-pick="#c4${source.id}arg${i}"><i class="ui icon minus"></i></button>
                                            </div>
                                        </g:each>
                                    </g:if>
                                </fieldset>
                                <button class="ui small button positive addArg" data-toForm="#c4${source.id}"><i class="ui icon plus"></i></button>
                            </td>
                            <td>
                                <button class="ui positive button">
                                    <g:message code="default.button.save"/>
                                </button>
                                <g:link class="ui button"
                                        controller="yoda"
                                        action="deleteStatsSource"
                                        params="${[counter4: source.id]}">${message('code':'default.button.delete.label')}</g:link>
                            </td>
                        </g:form>
                    </tr>
                </g:each>
                <g:each in="${c5sources}" var="source">
                    <tr>
                        <g:form action="editStatsSource" name="editStatsSource5" params="[c5source: source.id]">
                            <td>Counter 5 - ${source.id}</td>
                            <td>${source.provider.name}</td>
                            <td>${source.platform.name}</td>
                            <td>${source.baseUrl}</td>
                            <td>
                                <fieldset id="c5${source.id}">
                                    <g:if test="${source.arguments}">
                                        <g:set var="args" value="${JSON.parse(source.arguments)}"/>
                                        <g:each in="${args}" var="arg" status="i">
                                            <div class="ui input arg" id="c5${source.id}arg${i}">
                                                <input type="text" name="arg[]" value="${arg.getKey()}" placeholder="argument"/>
                                                <input type="text" name="val[]" value="${arg.getValue().value}" placeholder="value"/>
                                                <button class="ui small button negative remArg" data-pick="#c5${source.id}arg${i}"><i class="ui icon minus"></i></button>
                                            </div>
                                        </g:each>
                                    </g:if>
                                </fieldset>
                                <button class="ui small button positive addArg" data-toForm="#c5${source.id}"><i class="ui icon plus"></i></button>
                            </td>
                            <td>
                                <button class="ui positive button">
                                    <g:message code="default.button.save"/>
                                </button>
                                <g:link class="ui negative button"
                                        controller="yoda"
                                        action="deleteStatsSource"
                                        params="${[counter5: source.id]}">${message('code':'default.button.delete.label')}</g:link>
                            </td>
                        </g:form>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <semui:form>
            <g:form action="newStatsSource" class="ui form">

                <div class="fields three">
                    <div class="field">
                        <label><g:message code="stats.manage.counter.revision"/></label>
                        <div class="ui radio checkbox">
                            <input type="radio" name="version" value="counter4" class="hidden"/>
                            <label>v4</label>
                        </div>
                        <div class="ui radio checkbox">
                            <input type="radio" name="version" value="counter5" class="hidden"/>
                            <label>v5</label>
                        </div>
                    </div>
                    <div class="field">
                        <label><g:message code="default.provider.platform.label"/></label>
                        <g:select name="platform"
                                  from="${platforms}"
                                  required=""
                                  class="ui search dropdown"
                                  optionKey="id"
                                  optionValue="${{ it.org.name + (it.org.sortname ? " (${it.org.sortname})" : '') + ' : ' + it.name}}"
                        />
                    </div>
                    <div class="field">
                        <label>Base-URL</label>
                        <input type="url" name="baseUrl" placeholder="URL der Quelle"/>
                    </div>
                </div>

                <fieldset id="newSource">
                    <label>weitere Argumente</label>

                </fieldset>
                <button class="ui small button positive addArg" data-toForm="#newSource"><i class="ui icon plus"></i></button>

                <div class="field">
                    <label>&nbsp;</label>
                    <input type="submit" value="${message(code: 'default.button.submit.label')}" class="ui button"/>
                </div>

            </g:form>
        </semui:form>
        <laser:script file="${this.getGroovyPageFileName()}">
            $('body').on('click', '.addArg', function(e) {
                e.preventDefault();
                let attach = $(this).attr("data-toForm");
                let next = $(attach).find(".arg").length;
                let nextFields;
                if(attach === '#newSource')
                    nextFields = '<div class="fields one"><div class="ui input field arg" id="'+attach.substr(1)+'arg'+next+'"><input type="text" name="arg[]" placeholder="argument"/><input type="text" name="val[]" placeholder="value"/><button class="ui small button negative remArg" data-pick="'+attach+'arg'+next+'"><i class="ui icon minus"></i></button></div></div>';
                else
                    nextFields = '<div class="ui input arg" id="'+attach.substr(1)+'arg'+next+'"><input type="text" name="arg[]" placeholder="argument"/><input type="text" name="val[]" placeholder="value"/><button class="ui small button negative remArg" data-pick="'+attach+'arg'+next+'"><i class="ui icon minus"></i></button></div>';
                $(attach).append(nextFields);
            });

            $('body').on('click', '.remArg', function(e) {
                e.preventDefault();
                let detach = $(this).attr("data-pick");
                $(detach).remove();
            });
        </laser:script>
    </body>
</html>
