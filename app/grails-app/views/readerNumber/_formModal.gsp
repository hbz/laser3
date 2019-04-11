<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org;" %>
<laser:serviceInjection />

<semui:modal id="numbersFormModal" text="${message(code: 'readerNumber.create.label')}">

    <g:form class="ui form" id="create_number" url="[controller: 'numbers', action: 'create']" method="POST">
    <g:hiddenField name="orgid" value="${params.id}"/>

        <div class="field">
            <div class="two fields">

                <semui:datepicker class="wide eight" label="readerNumber.dueDate.label" id="dueDate" name="dueDate"
                                  placeholder="default.date.label" value="${numbersInstance?.dueDate}" required=""
                                  bean="${numbersInstance}"/>

                <semui:datepicker class="wide eight" label="readerNumber.semester.label" id="endDate" name="endDate"
                                  placeholder="default.date.label" value="${numbersInstance?.endDate}"
                                  bean="${numbersInstance}"/>

            </div>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide ten fieldcontain ${hasErrors(bean: numbersInstance, field: 'type', 'error')} required">
                    <label for="type">
                        <g:message code="readerNumber.referenceGroup.label" default="Type" />
                    </label>
                    <laser:select class="ui dropdown" id="type" name="type"
                                  from="${com.k_int.kbplus.ReaderNumber.getAllRefdataValues('Number Type')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${numbersInstance?.type?.id}"
                                  />

                </div>

                <div class="field wide six fieldcontain ${hasErrors(bean: numbersInstance, field: 'number', 'error')} required">
                    <label for="number">
                        <g:message code="readerNumber.number.label" default="Number" />

                    </label>
                    <g:textField id="number" name="number" value="${numbersInstance?.number}"/>

                </div>

            </div>
        </div>

    </g:form>

    <r:script>

        $('#create_number').form({
            on: 'blur',
            inline: true,
            fields: {
                number: {
                    identifier  : 'number',
                    rules: [
                        {
                            type   : 'number',
                            prompt : '{name} <g:message code="validation.onlyInteger" />'
                        }
                    ]
                },
                startDate: {
                    identifier : 'startDate',
                    rules: [
                        {
                            type : 'regExp',
                            value: /\d{2}\.\d{2}\.\d{4}/,
                            prompt: '<g:message code="validation.validDate"/>'
                        }
                    ]
                }
             }
        });

    </r:script>

</semui:modal>