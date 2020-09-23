<g:form action="public" controller="wcagTest" method="get" class="ui small form clearing">

    <div class="three fields">
        <div class="field fieldcontain">
            <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
        </div>

    </div>
</g:form>

<r:script>
        $(document).ready(function(){
              // initialize the form and fields
              $('.ui.form')
              .form();
            var val = "${params.dateBeforeFilter}";
            if(val == "null"){
                $(".dateBefore").addClass("hidden");
            }else{
                $(".dateBefore").removeClass("hidden");
            }
        });

        $("[name='dateBeforeFilter']").change(function(){
            var val = $(this)['context']['selectedOptions'][0]['label'];

            if(val != "${message(code:'default.filter.date.none', default:'-None-')}"){
                $(".dateBefore").removeClass("hidden");
            }else{
                $(".dateBefore").addClass("hidden");
            }
        })
</r:script>