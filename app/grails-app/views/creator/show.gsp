<%@ page import="com.k_int.kbplus.Creator" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'creator.label', default: 'Creator')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="row-fluid">

    <div class="span3">
        <div class="well">
            <ul class="nav nav-list">
                <li class="nav-header">${entityName}</li>
                <li>
                    <g:link class="list" action="list">
                        <i class="icon-list"></i>
                        <g:message code="default.list.label" args="[entityName]"/>
                    </g:link>
                </li>
                <li class="active">
                    <g:link class="create" action="create">
                        <i class="icon-plus icon-white"></i>
                        <g:message code="default.create.label" args="[entityName]"/>
                    </g:link>
                </li>
            </ul>
        </div>
    </div>

    <div class="span9">
        <h1 class="ui header"><semui:headerIcon/><g:message code="default.show.label" args="[entityName]"/></h1>

        <semui:messages data="${flash}"/>

        <div class="ui grid">

            <div class="twelve wide column">

                <div class="inline-lists">
                    <dl>

                        <dt><g:message code="creator.lastname.label" default="Lastname"/></dt>
                        <dd><semui:xEditable owner="${creatorInstance}" field="lastname"/></dd>

                        <dt><g:message code="creator.firstname.label" default="Firstname"/></dt>
                        <dd><semui:xEditable owner="${creatorInstance}" field="firstname"/></dd>


                        <dt><g:message code="creator.middlename.label" default="Middlename"/></dt>
                        <dd><semui:xEditable owner="${creatorInstance}" field="middlename"/></dd>


                        <dt><g:message code="creator.gnd_id.label" default="Gndid"/></dt>
                        <dd><g:if test="${editable}">
                            <semui:formAddIdentifier owner="${creatorInstance}" uniqueCheck="yes" onlyoneNamespace="GND"
                                                     uniqueWarningText="${message(code: 'subscription.details.details.duplicate.warn')}">
                                ${message(code: 'identifier.select.text', args: ['JC:66454'])}
                            </semui:formAddIdentifier>
                        </g:if>

                            <g:link controller="identifierOccurrence" action="show"
                                    id="${creatorInstance?.gnd_id?.id}">${creatorInstance?.gnd_id?.encodeAsHTML()}</g:link></dd>


                        <dt><g:message code="creator.globalUID.label" default="Global UID"/></dt>
                        <dd>${creatorInstance.globalUID}</dd>





                        <dt><g:message code="creator.title.label" default="Title"/></dt>

                        <dd><g:each in="${creatorInstance.title}" var="t">
                            <dl>
                                <dd>
                                    <g:link controller="TitleDetails" action="show"
                                            id="${t.title.id}">${t?.title.title}</g:link>
                                </dd>
                            </dl>
                        </g:each>

                            <dl>
                                <dt></dt>
                                <dd>
                                    <a class="ui button" data-semui="modal"
                                       href="#creatorTitle_add_modal">Titel hinzufügen</a>
                                    <g:render template="creatorTitleModal"
                                              model="${[creatorInstance: creatorInstance,
                                                        tmplText       : 'Titel hinzufügen']}"/>
                                </dd>
                            </dl>

                    </dl>

                    <g:form class="ui form">
                        <g:hiddenField name="id" value="${creatorInstance?.id}"/>
                        <div class="ui form-actions">
                            %{-- <g:link class="ui button" action="edit" id="${creatorInstance?.id}">
                                 <i class="write icon"></i>
                                 <g:message code="default.button.edit.label" default="Edit"/>
                             </g:link>--}%
                            <button class="ui button negative" type="submit" name="_action_delete">
                                <i class="trash alternate icon"></i>
                                <g:message code="default.button.delete.label" default="Delete"/>
                            </button>
                        </div>
                    </g:form>

                </div><!-- .twelve -->

            </div><!-- .grid -->
        </div>
    </div>
</body>
</html>
