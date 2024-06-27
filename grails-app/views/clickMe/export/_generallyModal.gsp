<laser:serviceInjection/>

<ui:modal modalSize="${modalSize}" id="${modalID}" text="${modalText}" refreshModal="true" hideSubmitButton="true">

    <g:if test="${clickMeConfig && clickMeConfig.note}">
        <ui:msg text="${clickMeConfig.note}" header="${g.message(code: 'default.note.label')}"/>
    </g:if>

    <g:form action="${exportAction}" controller="${exportController}" params="${exportParams}">
        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields,
                                                                                    filterFields: filterFields,
                                                                                    exportFileName: exportFileName,
                                                                                    contactSwitch: contactSwitch,
                                                                                    csvFieldSeparator: csvFieldSeparator,
                                                                                    orgSwitch: orgSwitch,
                                                                                    accessPointNotice: accessPointNotice,
                                                                                    currentTabNotice: currentTabNotice,
                                                                                    overrideFormat: overrideFormat,
                                                                                    showClickMeConfigSave: showClickMeConfigSave,
                                                                                    modalID: modalID]}"/>

    </g:form>

</ui:modal>