<%@ page import="de.laser.ExportClickMeService; de.laser.ImportService; de.laser.ui.Btn; de.laser.ui.Icon"%>
<g:uploadForm action="${processAction}" method="post">
    <g:if test="${subId}">
        <g:hiddenField name="subId" value="${subId}"/>
    </g:if>

    <ui:msg class="warning" header="${message(code: 'message.attention')}" text="" message="myinst.subscriptionImport.attention" showIcon="true" hideClose="true" />

    <div class="field">
        <div class="ui radio checkbox">
            <input id="formatXLS" name="format" type="radio" value="${ExportClickMeService.FORMAT.XLS.toString()}" class="hidden formatSelection" checked="checked">
            <label for="formatXLS"><g:message code="default.import.upload.xls"/></label>
        </div>
    </div>
    <div class="field">
        <div class="ui radio checkbox">
            <input id="formatCSV" name="format" type="radio" value="${ExportClickMeService.FORMAT.CSV.toString()}" class="hidden formatSelection">
            <label for="formatCSV"><g:message code="default.import.upload.csv"/></label>
        </div>
    </div>
    <div class="field">
        <div class="two fields">
            <div class="ui action input xls">
                <input type="text" readonly="readonly" class="ui input" placeholder="${message(code: 'myinst.subscriptionImport.fileSelectorXLS')}">

                <input type="file" name="excelFile" accept=".xls,.xlsx,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                       style="display: none;">
                <div class="${Btn.ICON.SIMPLE}">
                    <i class="${Icon.CMD.ATTACHMENT}"></i>
                </div>

                <button class="${Btn.SIMPLE}" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.uploadXLS"/></button>
            </div>
            <div class="ui action input csv">
                <input type="text" readonly="readonly" class="ui input" placeholder="${message(code: 'myinst.subscriptionImport.fileSelectorCSV')}">

                <input type="file" name="csvFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                       style="display: none;">
                <div class="${Btn.ICON.SIMPLE}">
                    <i class="${Icon.CMD.ATTACHMENT}"></i>
                </div>

                <select class="ui dropdown" name="separator">
                    <g:each in="${ImportService.CSV_CHARS}" var="setting">
                        <option value="${setting.charKey}"><g:message code="${setting.name}"/></option>
                    </g:each>
                </select>

                <button class="${Btn.SIMPLE}" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.uploadCSV"/></button>
            </div>
        </div>
    </div>
</g:uploadForm>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.csv').hide();

    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });

    $('.formatSelection').on('change', function() {
        if($(this).val() === '${ExportClickMeService.FORMAT.XLS}') {
            $('.xls').show();
            $('.csv').hide();
        }
        else if($(this).val() === '${ExportClickMeService.FORMAT.CSV}') {
            $('.csv').show();
            $('.xls').hide();
        }
    });
</laser:script>