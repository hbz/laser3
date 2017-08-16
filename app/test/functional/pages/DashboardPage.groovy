package pages

/**
 * Created by ioannis on 29/05/2014.
 */
class DashboardPage extends BasePage {
    static url = "/demo/home/index"
    static at = { browser.page.title.startsWith "KB+ Institutional Dash" };

    static content = {

        subscriptions {
            $("a", text: "Subscriptions").click(SubscrDetailsPage)
        }
        licenses {
            $("a", text: "Licenses").click(LicensePage)
        }
        toDo { ref ->
            $("a", text: ref).click(LicensePage)
        }
        generateWorksheet {
            $("a", text: "Generate Renewals Worksheet").click(MyInstitutionsPage)
        }
        importRenewals {
            $("a",text: "Import Renewals").click(MyInstitutionsPage)
        }

    }
}
