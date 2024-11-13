<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart message="menu.institutions.clickMeConfig" />

<ui:breadcrumbs>
    <ui:crumb message="menu.institutions.clickMeConfig" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon type="file" message="menu.institutions.clickMeConfig" total="${clickMeConfigsAllCount}" floated="true"/>
<br>
<br>
<br>
<br>
<ui:messages data="${flash}"/>


<div class="ui grid">
    <div class="four wide left attached column">
        <div class="ui vertical fluid tabular menu">
            <g:each in="${clickMeTypes.sort{message(code: "clickMeConfig.clickMeType.${it}")}}" var="clickMeTyp">
                <ui:tabsItem controller="myInstitution" action="exportConfigs"
                             params="${[tab: clickMeTyp]}" message="clickMeConfig.clickMeType.${clickMeTyp}" tab="${clickMeTyp}"
                             counts="${clickMeConfigsCount[clickMeTyp]}"/>
            </g:each>
        </div>
    </div>

    <div class="twelve wide stretched right attached column">
        <div class="ui seamless right attached segment">
            <table class="ui sortable celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.name.label')}</th>
                    <th>${message(code: 'default.note.label')}</th>
                    <th>${message(code: 'clickMeConfig.configOrder')}</th>
                    <g:if test="${editable}">
                        <th>${message(code: 'default.actions.label')}</th>
                    </g:if>
                </tr>
                </thead>
                <tbody>
                <g:each in="${clickMeConfigs}" var="clickMeConfig" status="i">
                    <tr>
                        <td>
                            ${(params.int('offset') ?: 0) + i + 1}
                        </td>
                        <td>
                            <ui:xEditable owner="${clickMeConfig}" field="name"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${clickMeConfig}" field="note" type="textarea"/>
                        </td>
                        <td>
                            <g:if test="${editable}">
                                <g:if test="${i == 1 && clickMeConfigs.size() == 2}">
                                    <g:link class="${Btn.MODERN.SIMPLE} compact" action="exportConfigsActions"
                                            params="[cmd: 'moveUp', id: clickMeConfig.id, tab: params.tab]"><i class="${Icon.CMD.MOVE_UP}"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:if test="${i > 0}">
                                        <g:link class="${Btn.MODERN.SIMPLE} compact" action="exportConfigsActions"
                                                params="[cmd: 'moveUp', id: clickMeConfig.id, tab: params.tab]"><i class="${Icon.CMD.MOVE_UP}"></i>
                                        </g:link>
                                    </g:if>
                                    <g:if test="${i < clickMeConfigs.size()-1}">
                                        <g:link class="${Btn.MODERN.SIMPLE} compact" action="exportConfigsActions"
                                                params="[cmd: 'moveDown', id: clickMeConfig.id, tab: params.tab]"><i class="${Icon.CMD.MOVE_DOWN}"></i>
                                        </g:link>
                                    </g:if>
                                </g:else>
                            </g:if>

                        </td>
                        <g:if test="${editable}">
                            <td>
                                <g:link controller="myInstitution" action="exportConfigsActions"
                                        params="${[cmd: 'delete', id: clickMeConfig.id, tab: params.tab]}" class="${Btn.MODERN.NEGATIVE}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </td>
                        </g:if>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</div>

<laser:htmlEnd/>
