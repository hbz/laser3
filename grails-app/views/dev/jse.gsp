<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : jsandco</title>
    <script>
        console.log('script @ head > 1')
    </script>
    <script>
        $(function(){
            console.log('script + $(doc).rdy() @ head > 2')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('asset:script @ head > 3')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('asset:script + $(doc).rdy() @ head > 4')
        })
    </asset:script>
</head>
<body>
    <g:render template="jse_tmpl" model="[pos:'oben']" />

    <script>
        console.log('script @ body > 5')
    </script>
    <script>
        $(function(){
            console.log('script + $(doc).rdy() @ body > 6')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('asset:script @ body > 7')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('asset:script + $(doc).rdy() @ body > 7')
        })
    </asset:script>

    <g:render template="jse_tmpl" model="[pos:'unten']" />

    <laser:remoteLink class="ui icon positive button" role="button"
                      controller="dev" action="jse" params="[xhr:true]"
                      data-before="console.log('-- XHR CALL --')"
                      data-update="jse_xhr"
    >XHR</laser:remoteLink>

    <div id="jse_xhr"></div>
</body>
</html>
