<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <script>
        console.log('[XHR] script @ head > 1')
    </script>
    <script>
        $(function(){
            console.log('[XHR] script + $(doc).rdy() @ head > 2')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('[XHR] asset:script @ head > 3')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('[XHR] asset:script + $(doc).rdy() @ head > 4')
        })
    </asset:script>
</head>
<body>
    <script>
        console.log('[XHR] script @ body > 5')
    </script>
    <script>
        $(function(){
            console.log('[XHR] script + $(doc).rdy() @ body > 6')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('[XHR] asset:script @ body > 7')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('[XHR] asset:script + $(doc).rdy() @ body > 8')
        })
    </asset:script>
    <p><strong>SUCCESS</strong></p>
</body>
</html>
