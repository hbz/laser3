
<g:set var="tt_params" value="${tt_params ?: [:]}" />

<ui:tabs actionName="${tt_action}">

    <ui:tabsItem controller="${tt_controller}" action="${tt_action}" params="${tt_params + [tab: "${tt_tabs[0]}"]}"
                 text="${message(code: "package.show.nav.current")}"
                 tab="${tt_tabs[0]}"
                 counts="${tt_counts[0]}"/>

    <ui:tabsItem controller="${tt_controller}" action="${tt_action}" params="${tt_params + [tab: "${tt_tabs[1]}"]}"
                 text="${message(code: "package.show.nav.planned")}"
                 tab="${tt_tabs[1]}"
                 counts="${tt_counts[1]}"/>

    <ui:tabsItem controller="${tt_controller}" action="${tt_action}" params="${tt_params + [tab: "${tt_tabs[2]}"]}"
                 text="${message(code: "package.show.nav.expired")}"
                 tab="${tt_tabs[2]}"
                 counts="${tt_counts[2]}"/>

    <ui:tabsItem controller="${tt_controller}" action="${tt_action}" params="${tt_params + [tab: "${tt_tabs[3]}"]}"
                 text="${message(code: "package.show.nav.deleted")}"
                 tab="${tt_tabs[3]}"
                 counts="${tt_counts[3]}"/>

    <ui:tabsItem controller="${tt_controller}" action="${tt_action}" params="${tt_params + [tab: "${tt_tabs[4]}"]}"
                 text="${message(code: "menu.public.all_titles")}"
                 tab="${tt_tabs[4]}"
                 counts="${tt_counts[4]}"/>

</ui:tabs>
