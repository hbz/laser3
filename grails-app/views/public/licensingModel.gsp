<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.UserSetting" %>
<laser:htmlStart text="Plan ">
    <style>
        .ui.table thead tr:first-child > th {
            top: 48px !important;
        }
        .ui.table thead tr:nth-child(2)>th {
            top: 119px !important;
        }
        .mk-test1:before {
            content: "\f328";
        }
        .mk-asService:before {
            content: "\f2d2";
        }
        .mk-accessRights:before {
            content: "\f505";
        }
        .mk-propertiesUse:before {
            content: "\f19c";
        }
        .mk-erms:before {
            content: "\f19c";
        }
        .mk-propertiesUse:before {
            content: "\f19c";
        }
        .mk-propertiesCreation:before {
            content: "\f19c";
        }
        .mk-cost:before {
            content: "\f155";
        }
        .mk-ie:before {
            content: "\f02d";
        }
        .mk-docs:before {
            content: "\f15c";
        }
        .mk-organisation:before {
            content: "\f0ae";
        }
        .mk-notifications:before {
            content: "\f024";
        }
        .mk-address:before {
            content: "\f0e0";
        }
        .mk-budget:before {
            content: "\f0d6";
        }
        .mk-reporting:before {
            content: "\f201";
        }
        .mk-management:before {
            content: "\e90f";
            font-family: icomoon;
        }
        .mk-community:before {
            content: "\e917";
            font-family: icomoon;
        }
        .mk-wekb:before {
            content: "\e90e";
            font-family: icomoon;
        }
        .mk-api:before {
            content: "\f120";
        }

        .mk-support:before {
            content: "\f51c";
        }
        .mk-help:before {
            content: "\e05c";
        }
        .mk-progression:before {
            content: "\f120";
        }
        .mk-trainingFundamentals:before {
            content: "\f51c";
        }
        .mk-trainingIndividual:before {
            content: "\f51c";
        }
        .mk-userMeeting:before {
            content: "\f51c";
        }

    </style>

</laser:htmlStart> %{-- </head><body>--}%
<div class="ui stackable grid">
    <div class="eleven wide column">
        <h1 class="ui icon header la-clear-before left floated aligned la-positionRelative"><i class="icon blue icon la-laser la-object"></i> Das LAS:eR Lizenzmodell</h1>
        <table class="ui large structured la-table table">
            <thead>
            <tr>
                <th class="two wide" rowspan="2">Leistungsmerkmale</th>
                <th class="center aligned"><h2 class="ui heading">Basic</h2>Mitgliedschaft</th>
                <th class="center aligned"><h2 class="ui heading">Pro</h2>Mitgliedschaft</th>
                <th class="center aligned"  rowspan="2">${message(code:'licensingModel.table.demo')}</th>
            </tr>

            <tr>
                <th class="two wide center aligned" colspan="2">
                    <a href="mailto:laser@hbz-nrw.de" class="ui huge first button" style="color: white">
                    Beratung vereinbaren<i class="right arrow icon"></i>
                    </a>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${mappingColsBasic}" var="mpg1">
                <tr>
                    <td class="tenwide">
                        <div class="ui list">
                            <div class="item">
                                <i class="icon la-list-icon mk-${mpg1}"></i>
                                <div class="content">
                                    <div class="header">${message(code:"marketing.featureList.major.${mpg1}")}</div>
                                    <div class="description">${message(code:"marketing.featureList.minor.${mpg1}-1")} </div>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg1}-2") != "marketing.featureList.minor.${mpg1}-2"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg1}-2")} </div>
                                    </g:if>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg1}-3") != "marketing.featureList.minor.${mpg1}-3"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg1}-3")} </div>
                                    </g:if>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg1}-4") != "marketing.featureList.minor.${mpg1}-4"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg1}-4")} </div>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </td>
                    <td class="four wide center aligned">
                        <i class="large green checkmark icon"></i>
                    </td>
                    <td class="four wide center aligned warning">
                        <i class="large green checkmark icon"></i>
                    </td>
                    <td class="center aligned">
                        <g:if test="${mpg1 == 'asService' }">
                            <button id="${mpg1}" class="ui icon blue button la-modal">
                                ${message(code:'licensingModel.button.watch')} <i class="film icon"></i>
                            </button>
                        </g:if>
                        <g:if test="${mpg1 == 'community' }">
                            <button id="${mpg1}" class="ui icon blue button la-modal">
                                ${message(code:'licensingModel.button.watch')} <i class="film icon"></i>
                            </button>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            <g:each in="${mappingColsPro}" var="mpg2">
                <tr>
                    <td class="ten wide">
                        <div class="ui list">
                            <div class="item">
                                <i class="icon la-list-icon mk-${mpg2}"></i>
                                <div class="content">
                                    <div class="header">${message(code:"marketing.featureList.major.${mpg2}")}</div>
                                    <div class="description">${message(code:"marketing.featureList.minor.${mpg2}-1")} </div>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg2}-2") != "marketing.featureList.minor.${mpg2}-2"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg2}-2")} </div>
                                    </g:if>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg2}-3") != "marketing.featureList.minor.${mpg2}-3"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg2}-3")} </div>
                                    </g:if>
                                    <g:if test="${message(code:"marketing.featureList.minor.${mpg2}-4") != "marketing.featureList.minor.${mpg2}-4"}">
                                        <div class="description">${message(code:"marketing.featureList.minor.${mpg2}-4")} </div>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </td>

                     <td class="four wide center aligned">

                     </td>
                    <td class="four wide center aligned warning">
                        <i class="large green checkmark icon"></i>
                    </td>
                    <td class="center aligned">
                        <g:if test="${mpg2 === 'management'}">
                            <button id="${mpg2}" class="ui icon blue button la-modal" >
                                ${message(code:'licensingModel.button.watch')} <i class="film icon"></i>
                            </button>
                        </g:if>
                        <g:if test="${mpg2 === 'reporting'}">
                            <button id="${mpg2}" class="ui icon blue button la-modal" >
                                ${message(code:'licensingModel.button.watch')} <i class="film icon"></i>
                            </button>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </div>
    <aside class="five wide column la-sidekick">

    </aside>
</div>
    <g:each in="${mappingColsPro+mappingColsBasic+mappingColsServiceBasic+mappingColsServicePro}" var="mpg5" >
        <g:if test="${mpg5 in ['asService','accessRight', 'community', 'management', 'reporting']}">

            <laser:script file="${this.getGroovyPageFileName()}">
                $('#${mpg5}').click(function(){
                    $.modal({
                        title: '${message(code:"marketing.featureList.major.${mpg5}")}<button class="ui right floated button la-animatedGif-redo ">Animation wiederholen</button>',
                        class: 'large',
                        closeIcon: true,
                        content: '<img width="100%" alt="" class="la-animatedGif-img la-padding-top-1em" src="${resource(dir: 'showcase', file: "${mpg5}.gif")}"/>',
                        actions: [{
                            text: '${message(code:"default.button.close.label")}',
                            class: 'green'
                        }]
                    }).modal('show');
                    if ($('.la-animatedGif-img').length) {
                          var gifSrc, srcGif;
                          $('.la-animatedGif-img').each(function () {
                            gifSrc = $(this).attr("src");
                            $(this).attr("data-gif", gifSrc);
                          });
                        }

                        $('.la-animatedGif-redo').on("click", function () {
                          srcGif = $(this).parent().next().find('.la-animatedGif-img ').attr("data-gif");
                          $(this).parent().next().find('.la-animatedGif-img ').attr("src", srcGif);
                    });
                });
            </laser:script>

        </g:if>
    </g:each>
<sec:ifAnyGranted roles="ROLE_USER">
    <style>
    .ui.table thead tr:first-child>th {
        top: 90px!important;
    }
     .ui.table thead tr:nth-child(2)>th {
         top: 160px!important;
     }
    </style>
</sec:ifAnyGranted>

<laser:htmlEnd />
