<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org;" %>
<laser:serviceInjection />

<semui:modal id="numbersFormModal" text="${message(code: 'numbers.create.label')}">

    <g:form class="ui form" id="create_number" url="[controller: 'numbers', action: 'create']" method="POST">


        <div class="field">
            <div class="two fields">

                <semui:datepicker class="wide eight" label="numbers.startDate.label" name="startDate"
                                  placeholder="default.date.label" value="${numbersInstance?.startDate}" required="true"
                                  bean="${numbersInstance}"/>

                <semui:datepicker class="wide eight" label="numbers.endDate.label" name="endDate"
                                  placeholder="default.date.label" value="${numbersInstance?.endDate}"
                                  bean="${numbersInstance}"/>

            </div>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide ten fieldcontain ${hasErrors(bean: numbersInstance, field: 'type', 'error')} required">
                    <label for="type">
                        <g:message code="numbers.type.label" default="Type" />
                    </label>
                    <laser:select class="ui dropdown" id="type" name="type"
                                  from="${com.k_int.kbplus.Numbers.getAllRefdataValues('Number Type')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${numbersInstance?.type?.id}"
                                  />

                </div>

                <div class="field wide six fieldcontain ${hasErrors(bean: numbersInstance, field: 'number', 'error')} ">
                    <label for="number">
                        <g:message code="numbers.number.label" default="Number" />

                    </label>
                    <g:textField name="number" value="${numbersInstance?.number}"/>

                </div>

            </div>
        </div>

    </g:form>

    <r:script>

        $('#create_person')
                .form({
            on: 'blur',
            inline: true,
            fields: {
                last_name: {
                    identifier  : 'last_name',
                    rules: [
                        {
                            type   : 'empty',
                            prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                        }
                    ]
                }
             }
        });

    </r:script>

</semui:modal>