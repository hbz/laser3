<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
</semui:breadcrumbs>

<br>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>

<g:render template="nav"/>


<semui:messages data="${flash}"/>


<h2><g:message code="surveyConfigsInfo.surveyConfig.info" args="[surveyConfig?.getConfigNameShort()]"/></h2>

<g:if test="${surveyConfig}">

    <div class="la-inline-lists">
        <div class="ui ${surveyConfig?.type == 'Subscription' ? 'three' : 'two'} stackable cards">

            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.type.label')}</dt>
                        <dd>
                            ${surveyConfig.getTypeInLocaleI10n()}

                            <g:if test="${surveyConfig?.surveyProperty}">

                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(surveyConfig?.surveyProperty?.type)}

                                <g:if test="${surveyConfig?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyConfig?.surveyProperty?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    (${refdataValues.join('/')})
                                </g:if>
                                </b>
                            </g:if>

                        </dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.orgIDs.label')}</dt>
                        <dd>
                            ${surveyConfig?.orgIDs?.size() ?: 0}
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.documents.label')}</dt>
                        <dd>
                            ${surveyConfig?.documents?.size()}
                        </dd>

                    </dl>
                </div>
            </div>
            <g:if test="${surveyConfig?.type == 'Subscription'}">
                <div class="ui card">
                    <div class="content">

                        <dl>
                            <dt class="control-label">${message(code: 'surveyConfig.costItemElement.label')}</dt>
                            <dd>
                                <semui:xEditableRefData config="CostItemElement" owner="${surveyConfig}"
                                                        field="costItemElement"/>
                            </dd>

                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'surveyConfig.priceComment.label')}</dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="priceComment" type="textarea"/>
                            </dd>

                        </dl>

                    </div>
                </div>
            </g:if>
            <div class="ui card ">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.header.label')}</dt>
                        <dd><semui:xEditable owner="${surveyConfig}" field="header"/></dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.comment.label')}</dt>
                        <dd><semui:xEditable owner="${surveyConfig}" field="comment" type="textarea"/></dd>

                    </dl>

                </div>
            </div>

        </div>
    </div>
</g:if>

<br>
<g:if test="${surveyConfig?.type == 'Subscription'}">
    <h3><g:message code="surveyConfigsInfo.surveyConfig.info2"/>

        <br>
        <g:link controller="subscription" action="show"
                id="${surveyConfig?.subscription?.id}">${surveyConfig?.subscription?.dropdownNamingConvention()}

            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyConfig?.type)}
        </g:link>

    </h3>


    <div>

        <g:form action="addSurveyConfigs" controller="survey" method="post" class="ui form">
            <g:hiddenField name="id" value="${surveyInfo?.id}"/>
            <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>

            <h4 class="ui left aligned icon header">${message(code: 'surveyProperty.plural.label')} <semui:totalNumber
                    total="${properties.size()}"/></h4>
            <table class="ui celled sortable table la-table">
                <thead>
                <tr>
                    <th class="left aligned"></th>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'surveyProperty.name.label')}</th>
                    <th>${message(code: 'surveyProperty.introduction.label')}</th>
                    <th>${message(code: 'surveyProperty.explain.label')}</th>
                    <th>${message(code: 'surveyProperty.comment.label')}</th>
                    <th>${message(code: 'surveyProperty.type.label')}</th>
                    <th></th>
                </tr>
                </thead>

                <g:each in="${properties}" var="property" status="i">
                    <tr>
                        <td>
                            <g:if test="${com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, property)}">
                                <i class="check circle icon green"></i>
                            </g:if>
                            <g:else>
                                <g:checkBox name="selectedProperty" value="${property.id}" checked="false"/>
                            </g:else>
                        </td>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <td>
                            ${property?.getI10n('name')}
                        </td>
                        <td>
                            <g:if test="${property?.getI10n('introduction')}">
                                ${property?.getI10n('introduction')}
                            </g:if>
                        </td>

                        <td>
                            <g:if test="${property?.getI10n('explain')}">
                                ${property?.getI10n('explain')}
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${property?.getI10n('comment')}">
                                ${property?.getI10n('comment')}
                            </g:if>
                        </td>
                        <td>
                            ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(property?.type)}

                            <g:if test="${property?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                <g:set var="refdataValues" value="${[]}"/>
                                <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(property?.refdataCategory)}"
                                        var="refdataValue">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                </g:each>
                                <br>
                                (${refdataValues.join('/')})
                            </g:if>

                        </td>
                        <td>
                            <g:if test="${editable && com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, property)}">
                                <g:link class="ui icon negative button"
                                        controller="survey" action="deleteSurveyPropfromSub"
                                        id="${property?.id}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:if>

                        </td>
                    </tr>
                </g:each>
            </table>


            <input type="submit" class="ui button"
                   value="${message(code: 'surveyConfigsInfo.add.button')}"/>

        </g:form>

    </div>
</g:if>


