<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
    <g:render template="public_navbar" contextPath="/templates" model="['active': 'home']"/>

    <div class="ui container center aligned">


        <div class="ui grid ">
            <div class="four wide column "></div>

            <div class="four wide column ">
                <div class="ui card">
                    <div class="content">
                        <div class="header">Informationen zu LAS:eR</div>

                        <br>
                        <p>
                            <a href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="ui fluid button">
                                LAS:eR wiki
                            </a>
                        </p>
                    </div>
                </div>

            </div>

            <div class="four wide column">
                <g:render template="/templates/loginDiv"/>
            </div>
            <div class="four wide column "></div>
        </div>
    </div>
</body>
</html>
