<%@page import="grails.converters.JSON; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants;" %>
<laser:serviceInjection/>

<div class="ui top attached segment" id="chartContainer">
    <h2>Meine Subskriptionen</h2>
    <a class="closeSegment" data-entry="eee">
        <i class="ui icon times"></i>
    </a>
</div>

<g:applyCodec encodeAs="none">
    <script>
        $(document).ready(function(){
            let result, labels, seriesObj, series

            $("#chartContainer").on('click','.closeSegment',function() {
                $('div[data-entry="'+$(this).attr('data-entry')+'"]').remove();
            });
        });
    </script>
</g:applyCodec>