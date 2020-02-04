import de.laser.helper.RDConstants

//following code ex MyInstitutionController.groovy, was as-is also in SubscriptionImportController which has been deleted including respective views

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalsSearch() {

        log.debug("renewalsSearch : ${params}");
        log.debug("Start year filters: ${params.startYear}");


        // Be mindful that the behavior of this controller is strongly influenced by the schema setup in ES.
        // Specifically, see KBPlus/import/processing/processing/dbreset.sh for the mappings that control field type and analysers
        // Internal testing with http://localhost:9200/kbplus/_search?q=subtype:'Subscription%20Offered'
        Map<String, Object> result = [:]

        result.institution = contextService.getOrg()
        result.user = springSecurityService.getCurrentUser()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[(result?.institution?.name?: message(code:'myinst.error.noMember.ph', default:'the selected institution'))]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${result.institution.name}. Please request access on the profile page");
            return;
        }

        if (params.searchreset) {
            params.remove("pkgname")
            params.remove("search")
        }

        def shopping_basket = UserFolder.findByUserAndShortcode(result.user, 'RenewalsBasket') ?: new UserFolder(user: result.user, shortcode: 'RenewalsBasket').save();

        shopping_basket.save(flush: true)

        if (params.addBtn) {
            log.debug("Add item ${params.addBtn} to basket");
            def oid = "com.k_int.kbplus.Package:${params.addBtn}"
            shopping_basket.addIfNotPresent(oid)
            shopping_basket.save(flush: true);
        } else if (params.clearBasket == 'yes') {
            log.debug("Clear basket....");
            shopping_basket.removePackageItems();
            shopping_basket.save(flush: true)
        } else if (params.clearOnlyoneitemBasket) {
            log.debug("Clear item ${params.clearOnlyoneitemBasket} from basket ");
            def oid = "com.k_int.kbplus.Package:${params.clearOnlyoneitemBasket}"
            shopping_basket.removeItem(oid)
            shopping_basket.save(flush: true)
        } else if (params.generate == 'yes') {
            log.debug("Generate");
            generate(materialiseFolder(shopping_basket.items), result.institution)
            return
        }

        result.basket = materialiseFolder(shopping_basket.items)


        //Following are the ES stuff
        try {
            StringWriter sw = new StringWriter()
            def fq = null;
            boolean has_filter = false
            //This handles the facets.
            params.each { p ->
                if (p.key.startsWith('fct:') && p.value.equals("on")) {
                    log.debug("start year ${p.key} : -${p.value}-");

                    if (!has_filter)
                        has_filter = true;
                    else
                        sw.append(" AND ");

                    String[] filter_components = p.key.split(':');
                    switch (filter_components[1]) {
                        case 'consortiaName':
                            sw.append('consortiaName')
                            break;
                        case 'startYear':
                            sw.append('startYear')
                            break;
                        case 'endYear':
                            sw.append('endYear')
                            break;
                        case 'cpname':
                            sw.append('cpname')
                            break;
                    }
                    if (filter_components[2].indexOf(' ') > 0) {
                        sw.append(":\"");
                        sw.append(filter_components[2])
                        sw.append("\"");
                    } else {
                        sw.append(":");
                        sw.append(filter_components[2])
                    }
                }
                if ((p.key.startsWith('fct:')) && p.value.equals("")) {
                    params.remove(p.key)
                }
            }

            if (has_filter) {
                fq = sw.toString();
                log.debug("Filter Query: ${fq}");
            }
            params.sort = "name"
            params.rectype = "Package" // Tells ESSearchService what to look for
            if(params.pkgname) params.q = params.pkgname
            if(fq){
                if(params.q) parms.q += " AND ";
                params.q += " (${fq}) ";
            }
            result += ESSearchService.search(params)
        }
        catch (Exception e) {
            log.error("problem", e);
        }

        result.basket.each
                {
                    if (it instanceof Subscription) result.sub_id = it.id
                }

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def selectPackages() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)

        result.candidates = [:]
        def title_list = []
        def package_list = []

        result.titles_in_this_sub = result.subscriptionInstance.issueEntitlements.size();

        result.subscriptionInstance.issueEntitlements.each { e ->
            def title = e.tipp.title
            log.debug("Looking for packages offering title ${title.id} - ${title?.title}");

            title.tipps.each { t ->
                log.debug("  -> This title is provided by package ${t.pkg.id} on platform ${t.platform.id}");

                def title_idx = title_list.indexOf("${title.id}");
                def pkg_idx = package_list.indexOf("${t.pkg.id}:${t.platform.id}");

                if (title_idx == -1) {
                    // log.debug("  -> Adding title ${title.id} to matrix result");
                    title_list.add("${title.id}");
                    title_idx = title_list.size();
                }

                if (pkg_idx == -1) {
                    log.debug("  -> Adding package ${t.pkg.id} to matrix result");
                    package_list.add("${t.pkg.id}:${t.platform.id}");
                    pkg_idx = package_list.size();
                }

                log.debug("  -> title_idx is ${title_idx} pkg_idx is ${pkg_idx}");

                def candidate = result.candidates["${t.pkg.id}:${t.platform.id}"]
                if (!candidate) {
                    candidate = [:]
                    result.candidates["${t.pkg.id}:${t.platform.id}"] = candidate;
                    candidate.pkg = t.pkg.id
                    candidate.platform = t.platform
                    candidate.titlematch = 0
                    candidate.pkg = t.pkg
                    candidate.pkg_title_count = t.pkg.tipps.size();
                }
                candidate.titlematch++;
                log.debug("  -> updated candidate ${candidate}");
            }
        }

        log.debug("titles list ${title_list}");
        log.debug("package list ${package_list}");

        log.debug("titles list size ${title_list.size()}");
        log.debug("package list size ${package_list.size()}");
        result
    }

    private def buildRenewalsQuery(params) {
        log.debug("BuildQuery...");

        StringWriter sw = new StringWriter()

        // sw.write("subtype:'Subscription Offered'")
        sw.write("rectype:'Package'")

        renewals_reversemap.each { mapping ->

            // log.debug("testing ${mapping.key}");

            if (params[mapping.key] != null) {
                if (params[mapping.key].class == java.util.ArrayList) {
                    params[mapping.key].each { p ->
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${p}\"")
                    }
                } else {
                    // Only add the param if it's length is > 0 or we end up with really ugly URLs
                    // II : Changed to only do this if the value is NOT an *
                    if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${params[mapping.key]}\"")
                    }
                }
            }
        }


        def result = sw.toString();
        result;
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def materialiseFolder(f) {
        def result = []
        f.each {
            def item_to_add = genericOIDService.resolveOID(it.referencedOid)
            if (item_to_add) {
                result.add(item_to_add)
            } else {
                flash.message = message(code:'myinst.materialiseFolder.error', default:'Folder contains item that cannot be found');
            }
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def generate(plist, inst) {
        try {
            def m = generateMatrix(plist, inst)
            exportWorkbook(m, inst)
        }
        catch (Exception e) {
            log.error("Problem", e);
            response.sendError(500)
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def generateMatrix(plist, inst) {

        def titleMap = [:]
        def subscriptionMap = [:]

        log.debug("pre-pre-process");

        boolean first = true;

        def formatter = DateUtil.getSDF_NoTime()

        // Add in JR1 and JR1a reports
        def c = new GregorianCalendar()
        c.setTime(new Date());
        def current_year = c.get(Calendar.YEAR)

        // Step one - Assemble a list of all titles and packages.. We aren't assembling the matrix
        // of titles x packages yet.. Just gathering the data for the X and Y axis
        plist.each { sub ->


            log.debug("pre-pre-process (Sub ${sub.id})");

            def sub_info = [
                    sub_idx : subscriptionMap.size(),
                    sub_name: sub.name,
                    sub_id : "${sub.class.name}:${sub.id}"
            ]

            subscriptionMap[sub.id] = sub_info

            // For each subscription in the shopping basket
            if (sub instanceof Subscription) {
                log.debug("Handling subscription: ${sub_info.sub_name}")
                sub_info.putAll([sub_startDate : sub.startDate ? formatter.format(sub.startDate):null,
                sub_endDate: sub.endDate ? formatter.format(sub.endDate) : null])
                sub.issueEntitlements.each { ie ->
                    // log.debug("IE");
                    if (!(ie.status?.value == 'Deleted')) {
                        def title_info = titleMap[ie.tipp.title.id]
                        if (!title_info) {
                            log.debug("Adding ie: ${ie}");
                            title_info = [:]
                            title_info.title_idx = titleMap.size()
                            title_info.id = ie.tipp.title.id;
                            title_info.issn = ie.tipp.title.getIdentifierValue('ISSN');
                            title_info.eissn = ie.tipp.title.getIdentifierValue('eISSN');
                            title_info.title = ie.tipp.title.title
                            if (first) {
                                if (ie.startDate)
                                    title_info.current_start_date = formatter.format(ie.startDate)
                                if (ie.endDate)
                                    title_info.current_end_date = formatter.format(ie.endDate)
                                title_info.current_embargo = ie.embargo
                                title_info.current_depth = ie.coverageDepth
                                title_info.current_coverage_note = ie.coverageNote
                                def test_coreStatus =ie.coreStatusOn(new Date())
                                def formatted_date = formatter.format(new Date())
                                title_info.core_status = test_coreStatus?"True(${formatted_date})": test_coreStatus==null?"False(Never)":"False(${formatted_date})"
                                title_info.core_status_on = formatted_date
                                title_info.core_medium = ie.coreStatus


                                /*try {
                                    log.debug("get jusp usage");
                                    title_info.jr1_last_4_years = factService.lastNYearsByType(title_info.id,
                                            inst.id,
                                            ie.tipp.pkg.contentProvider.id, 'STATS:JR1', 4, current_year)

                                    title_info.jr1a_last_4_years = factService.lastNYearsByType(title_info.id,
                                            inst.id,
                                            ie.tipp.pkg.contentProvider.id, 'STATS:JR1a', 4, current_year)
                                }
                                catch (Exception e) {
                                    log.error("Problem collating STATS report info for title ${title_info.id}", e);
                                }*/

                                // log.debug("added title info: ${title_info}");
                            }
                            titleMap[ie.tipp.title.id] = title_info;
                        }
                    }
                }
            } else if (sub instanceof Package) {
                log.debug("Adding package into renewals worksheet");
                sub.tipps.each { tipp ->
                    log.debug("Package tipp");
                    if (!(tipp.status?.value == 'Deleted')) {
                        def title_info = titleMap[tipp.title.id]
                        if (!title_info) {
                            // log.debug("Adding ie: ${ie}");
                            title_info = [:]
                            title_info.title_idx = titleMap.size()
                            title_info.id = tipp.title.id;
                            title_info.issn = tipp.title.getIdentifierValue('ISSN');
                            title_info.eissn = tipp.title.getIdentifierValue('eISSN');
                            title_info.title = tipp.title.title
                            titleMap[tipp.title.id] = title_info;
                        }
                    }
                }
            }

            first = false
        }

        log.debug("Result will be a matrix of size ${titleMap.size()} by ${subscriptionMap.size()}");

        // Object[][] result = new Object[subscriptionMap.size()+1][titleMap.size()+1]
        Object[][] ti_info_arr = new Object[titleMap.size()][subscriptionMap.size()]
        Object[] sub_info_arr = new Object[subscriptionMap.size()]
        Object[] title_info_arr = new Object[titleMap.size()]

        // Run through the list of packages, and set the X axis headers accordingly
        subscriptionMap.values().each { v ->
            sub_info_arr[v.sub_idx] = v
        }

        // Run through the titles and set the Y axis headers accordingly
        titleMap.values().each { v ->
            title_info_arr[v.title_idx] = v
        }

        // Fill out the matrix by looking through each sub/package and adding the appropriate cell info
        plist.each { sub ->
            def sub_info = subscriptionMap[sub.id]
            if (sub instanceof Subscription) {
                log.debug("Filling out renewal sheet column for an ST");
                sub.issueEntitlements.each { ie ->
                    if (!(ie.status?.value == 'Deleted')) {
                        def title_info = titleMap[ie.tipp.title.id]
                        def ie_info = [:]
                        // log.debug("Adding tipp info ${ie.tipp.startDate} ${ie.tipp.derivedFrom}");
                        ie_info.tipp_id = ie.tipp.id;
                        def test_coreStatus =ie.coreStatusOn(new Date())
                        def formatted_date = formatter.format(new Date())
                        ie_info.core_status = test_coreStatus?"True(${formatted_date})": test_coreStatus==null?"False(Never)":"False(${formatted_date})"
                        ie_info.core_status_on = formatted_date
                        ie_info.core_medium = ie.coreStatus
                        ie_info.startDate_d = ie.tipp.startDate ?: ie.tipp.derivedFrom?.startDate
                        ie_info.startDate = ie_info.startDate_d ? formatter.format(ie_info.startDate_d) : null
                        ie_info.startVolume = ie.tipp.startVolume ?: ie.tipp.derivedFrom?.startVolume
                        ie_info.startIssue = ie.tipp.startIssue ?: ie.tipp.derivedFrom?.startIssue
                        ie_info.endDate_d = ie.endDate ?: ie.tipp.derivedFrom?.endDate
                        ie_info.endDate = ie_info.endDate_d ? formatter.format(ie_info.endDate_d) : null
                        ie_info.endVolume = ie.endVolume ?: ie.tipp.derivedFrom?.endVolume
                        ie_info.endIssue = ie.endIssue ?: ie.tipp.derivedFrom?.endIssue

                        ti_info_arr[title_info.title_idx][sub_info.sub_idx] = ie_info
                    }
                }
            } else if (sub instanceof Package) {
                log.debug("Filling out renewal sheet column for a package");
                sub.tipps.each { tipp ->
                    if (!(tipp.status?.value == 'Deleted')) {
                        def title_info = titleMap[tipp.title.id]
                        def ie_info = [:]
                        // log.debug("Adding tipp info ${tipp.startDate} ${tipp.derivedFrom}");
                        ie_info.tipp_id = tipp.id;
                        ie_info.startDate_d = tipp.startDate
                        ie_info.startDate = ie_info.startDate_d ? formatter.format(ie_info.startDate_d) : null
                        ie_info.startVolume = tipp.startVolume
                        ie_info.startIssue = tipp.startIssue
                        ie_info.endDate_d = tipp.endDate
                        ie_info.endDate = ie_info.endDate_d ? formatter.format(ie_info.endDate_d) : null
                        ie_info.endVolume = tipp.endVolume ?: tipp.derivedFrom?.endVolume
                        ie_info.endIssue = tipp.endIssue ?: tipp.derivedFrom?.endIssue

                        ti_info_arr[title_info.title_idx][sub_info.sub_idx] = ie_info
                    }
                }
            }
        }

        log.debug("Completed.. returning final result");

        def final_result = [
                ti_info     : ti_info_arr,                      // A crosstab array of the packages where a title occours
                title_info  : title_info_arr,                // A list of the titles
                sub_info    : sub_info_arr,
                current_year: current_year]                  // The subscriptions offered (Packages)

        return final_result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def exportWorkbook(m, inst) {
        try {
            log.debug("export workbook");

            // read http://stackoverflow.com/questions/2824486/groovy-grails-how-do-you-stream-or-buffer-a-large-file-in-a-controllers-respon
            def date = new Date()
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy")

            XSSFWorkbook wb = new XSSFWorkbook()
            POIXMLProperties xmlProps = wb.getProperties()
            POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
            coreProps.setCreator(message(code:'laser'))
            SXSSFWorkbook workbook = new SXSSFWorkbook(wb,50,true)

            CreationHelper factory = workbook.getCreationHelper()

            //
            // Create two sheets in the excel document and name it First Sheet and
            // Second Sheet.
            //
            SXSSFSheet firstSheet = workbook.createSheet(g.message(code: "renewalexport.renewalsworksheet", default: "Renewals Worksheet"))
            Drawing drawing = firstSheet.createDrawingPatriarch()

            // Cell style for a present TI
            XSSFCellStyle present_cell_style = workbook.createCellStyle()
            present_cell_style.setFillForegroundColor(new XSSFColor(new Color(198,239,206)))
            present_cell_style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND)

            // Cell style for a core TI
            XSSFCellStyle core_cell_style = workbook.createCellStyle()
            core_cell_style.setFillForegroundColor(new XSSFColor(new Color(255,235,156)))
            core_cell_style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND)

            //NOT CHANGE
            XSSFCellStyle notchange_cell_style = workbook.createCellStyle()
            notchange_cell_style.setFillForegroundColor(new XSSFColor(new Color(255,199,206)))
            notchange_cell_style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND)

            int rc = 0
            // header
            int cc = 0
            Row row = null
            Cell cell = null

            log.debug(m.sub_info.toString())

            // Blank rows
            row = firstSheet.createRow(rc++)
            row = firstSheet.createRow(rc++)
            cc = 0
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.subscriberID", default: "Subscriber ID"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.subscribername", default: "Subscriber Name"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.subscriberShortcode", default: "Subscriber Shortcode"))

            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.subscriptionStartDate", default: "Subscription Startdate"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.subscriptionEndDate", default: "Subscription Enddate"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.copySubscriptionDoc", default: "Copy Subscription Doc"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.generated", default: "Generated at"))

            row = firstSheet.createRow(rc++)
            cc = 0
            cell = row.createCell(cc++)
            cell.setCellValue(inst.id)
            cell.setCellStyle(notchange_cell_style)
            cell = row.createCell(cc++)
            cell.setCellValue(inst.name)
            cell = row.createCell(cc++)
            cell.setCellValue(inst.shortcode)

            def subscription = m.sub_info.find{it.sub_startDate}
            cell = row.createCell(cc++)
            cell.setCellValue(subscription?.sub_startDate?:'')
            cell = row.createCell(cc++)
            cell.setCellValue(subscription?.sub_endDate?:'')
            cell = row.createCell(cc++)
            cell.setCellValue(subscription?.sub_id?:m.sub_info[0].sub_id)
            cell.setCellStyle(notchange_cell_style)
            cell = row.createCell(cc++)
            cell.setCellValue(sdf.format(date))
            row = firstSheet.createRow(rc++)

            // Key
            row = firstSheet.createRow(rc++)
            cc = 0
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.key", default: "Key"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.inSubscription", default: "In Subscription"))
            cell.setCellStyle(present_cell_style)
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.coreTitle", default: "Core Title"))
            cell.setCellStyle(core_cell_style)
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.notinSubscription", default: "Not in Subscription"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.notChange", default: "Not Change"))
            cell.setCellStyle(notchange_cell_style)
            cell = row.createCell(21)
            cell.setCellValue(g.message(code: "renewalexport.currentSub", default: "Current Subscription"))
            cell = row.createCell(22)
            cell.setCellValue(g.message(code: "renewalexport.candidates", default: "Candidates"))


            row = firstSheet.createRow(rc++)
            cc = 21
            m.sub_info.each { sub ->
                cell = row.createCell(cc++)
                cell.setCellValue(sub.sub_id)
                cell.setCellStyle(notchange_cell_style)
            }

            // headings
            row = firstSheet.createRow(rc++)
            cc = 0
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.titleID", default: "Title ID"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.title", default: "Title"))
            cell = row.createCell(cc++)
            cell.setCellValue("ISSN")
            cell = row.createCell(cc++)
            cell.setCellValue("eISSN")
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.currentStartDate", default: "Current Startdate"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.currentEndDate", default: "Current Enddate"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.currentCoverageDepth", default: "Current Coverage Depth"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.currentCoverageNote", default: "Current Coverage Note"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.coreStatus", default: "Core Status"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.coreStatusCheked", default: "Core Status Checked"))
            cell = row.createCell(cc++)
            cell.setCellValue(g.message(code: "renewalexport.coreMedium", default: "Core Medium"))

            // USAGE History
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1\n${m.current_year - 4}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1a\n${m.current_year - 4}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1\n${m.current_year - 3}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1a\n${m.current_year - 3}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1\n${m.current_year - 2}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1a\n${m.current_year - 2}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1\n${m.current_year - 1}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1a\n${m.current_year - 1}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1\n${m.current_year}")
            cell = row.createCell(cc++)
            cell.setCellValue("Nationale Statistik JR1a\n${m.current_year}")

            m.sub_info.each { sub ->
                cell = row.createCell(cc++)
                cell.setCellValue(sub.sub_name)

                // Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
                // link.setAddress("http://poi.apache.org/");
                // cell.setHyperlink(link);
            }

            m.title_info.each { title ->

                row = firstSheet.createRow(rc++);
                cc = 0;

                // Internal title ID
                cell = row.createCell(cc++)
                cell.setCellValue(title.id)
                // Title
                cell = row.createCell(cc++)
                cell.setCellValue(title.title ?: '')

                // ISSN
                cell = row.createCell(cc++)
                cell.setCellValue(title.issn ?: '')

                // eISSN
                cell = row.createCell(cc++)
                cell.setCellValue(title.eissn ?: '')

                // startDate
                cell = row.createCell(cc++)
                cell.setCellValue(title.current_start_date ?: '')

                // endDate
                cell = row.createCell(cc++)
                cell.setCellValue(title.current_end_date ?: '')

                // coverageDepth
                cell = row.createCell(cc++)
                cell.setCellValue(title.current_depth ?: '')

                // embargo
                cell = row.createCell(cc++)
                cell.setCellValue(title.current_coverage_note ?: '')

                // IsCore
                cell = row.createCell(cc++)
                cell.setCellValue(title.core_status ?: '')

                // Core Start Date
                cell = row.createCell(cc++)
                cell.setCellValue(title.core_status_on ?: '')

                // Core End Date
                cell = row.createCell(cc++)
                cell.setCellValue(title.core_medium ?: '')

                // Usage Stats
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1_last_4_years[4] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1a_last_4_years[4] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1_last_4_years[3] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1a_last_4_years[3] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1_last_4_years[2] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1a_last_4_years[2] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1_last_4_years[1] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1a_last_4_years[1] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1_last_4_years[0] ?: '0')
                cell = row.createCell(cc++)
                if (title.jr1_last_4_years)
                    cell.setCellValue(title.jr1a_last_4_years[0] ?: '0')

                m.sub_info.each { sub ->

                    cell = row.createCell(cc++);
                    def ie_info = m.ti_info[title.title_idx][sub.sub_idx]
                    if (ie_info) {
                        if ((ie_info.core_status) && (ie_info.core_status.contains("True"))) {
                            cell.setCellValue("")
                            cell.setCellStyle(core_cell_style);
                        } else {
                            cell.setCellValue("")
                            cell.setCellStyle(present_cell_style);
                        }
                        if (sub.sub_idx == 0) {
                            addCellComment(row, cell, "${title.title} ${g.message(code: "renewalexport.providedby", default: "provided by")} ${sub.sub_name}\n${g.message(code: "renewalexport.startDate", default: "Start Date")}:${ie_info.startDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startVolume", default: "Start Volume")}:${ie_info.startVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startIssue", default: "Start Issue")}:${ie_info.startIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endDate", default: "End Date")}:${ie_info.endDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endVolume", default: "End Volume")}:${ie_info.endVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endIssue", default: "End Issue")}:${ie_info.endIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n", drawing, factory);
                        } else {
                            addCellComment(row, cell, "${title.title} ${g.message(code: "renewalexport.providedby", default: "provided by")} ${sub.sub_name}\n${g.message(code: "renewalexport.startDate", default: "Start Date")}:${ie_info.startDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startVolume", default: "Start Volume")}:${ie_info.startVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startIssue", default: "Start Issue")}:${ie_info.startIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endDate", default: "End Date")}:${ie_info.endDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endVolume", default: "End Volume")}:${ie_info.endVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endIssue", default: "End Issue")}:${ie_info.endIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.selectTitle", default: "Select Title by setting this cell to Y")}\n", drawing, factory);
                        }
                    }

                }
            }
            row = firstSheet.createRow(rc++);
            cell = row.createCell(0);
            cell.setCellValue("END")

            // firstSheet.autoSizeRow(6); //adjust width of row 6 (Headings for JUSP Stats)
//            Row jusp_heads_row = firstSheet.getRow(6);
//            jusp_heads_row.setHeight((short) (jusp_heads_row.getHeight() * 2));

            for (int i = 0; i < 22; i++) {
                firstSheet.autoSizeColumn(i); //adjust width of the first column
            }
            for (int i = 0; i < m.sub_info.size(); i++) {
                firstSheet.autoSizeColumn(22 + i); //adjust width of the second column
            }

            response.setHeader "Content-disposition", "attachment; filename=\"${message(code: "renewalexport.renewals")}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        catch (Exception e) {
            log.error("Problem", e);
            response.sendError(500)
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalsnoPackageChange() {
        def result = setResultGenerics()

        if (!accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code: 'myinst.error.noMember', args: [result.institution.name]);
            response.sendError(401)
            return;
        } else if (!accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code: 'myinst.renewalUpload.error.noAdmin', default: 'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')

        def subscription = Subscription.get(params.sub_id)

        result.errors = []

        result.permissionInfo = [sub_startDate: (subscription.startDate ? sdf.format(subscription.startDate) : null), sub_endDate: (subscription.endDate ? sdf.format(subscription.endDate) : null), sub_name: subscription.name, sub_id: subscription.id, sub_license: subscription?.owner?.reference?:'']

        result.entitlements = subscription.issueEntitlements

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalswithoutPackage() {
        def result = setResultGenerics()

        if (!accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code: 'myinst.error.noMember', args: [result.institution.name]);
            response.sendError(401)
            return;
        } else if (!accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code: 'myinst.renewalUpload.error.noAdmin', default: 'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')

        def subscription = Subscription.get(params.sub_id)

        result.errors = []

        result.permissionInfo = [sub_startDate: (subscription.startDate ? sdf.format(subscription.startDate) : null), sub_endDate: (subscription.endDate ? sdf.format(subscription.endDate) : null), sub_name: subscription.name, sub_id: subscription.id, sub_license: subscription?.owner?.reference?:'']

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def renewalsUpload() {
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            return;
        } else if (! accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code:'myinst.renewalUpload.error.noAdmin', default:'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        result.errors = []

        log.debug("upload");

        if (request.method == 'POST') {
            def upload_mime_type = request.getFile("renewalsWorksheet")?.contentType
            def upload_filename = request.getFile("renewalsWorksheet")?.getOriginalFilename()
            log.debug("Uploaded worksheet type: ${upload_mime_type} filename was ${upload_filename}");
            def input_stream = request.getFile("renewalsWorksheet")?.inputStream
            if (input_stream.available() != 0) {
                processRenewalUpload(input_stream, upload_filename, result)
            } else {
                flash.error = message(code:'myinst.renewalUpload.error.noSelect');
            }
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processRenewalUpload(input_stream, upload_filename, result) {
        int SO_START_COL = 22
        int SO_START_ROW = 7
        log.debug("processRenewalUpload - opening upload input stream as HSSFWorkbook");
        def user = User.get(springSecurityService.principal.id)

        if (input_stream) {
            XSSFWorkbook wb
            try {
                wb = new XSSFWorkbook(input_stream)
            } catch (IOException e) {
                if (e.getMessage().contains("Invalid header signature")) {
                    flash.error = message(code:'myinst.processRenewalUpload.error.invHeader', default:'Error creating workbook. Possible causes: document format, corrupted file.')
                } else {
                    flash.error = message(code:'myinst.processRenewalUpload.error', default:'Error creating workbook')
                }
                log.debug("Error creating workbook from input stream. ", e)
                return result;
            }
            XSSFSheet firstSheet = wb.getSheetAt(0);

            SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')

            // Step 1 - Extract institution id, name and shortcode
            Row org_details_row = firstSheet.getRow(2)
            String org_name = org_details_row?.getCell(0)?.toString()
            String org_id = org_details_row?.getCell(1)?.toString()
            String org_shortcode = org_details_row?.getCell(2)?.toString()
            String sub_startDate = org_details_row?.getCell(3)?.toString()
            String sub_endDate = org_details_row?.getCell(4)?.toString()
            String original_sub_id = org_details_row?.getCell(5)?.toString()
            def original_sub = null
            if(original_sub_id){
                original_sub = genericOIDService.resolveOID(original_sub_id)
                if(!original_sub.hasPerm("view",user)){
                    original_sub = null;
                    flash.error = message(code:'myinst.processRenewalUpload.error.access')
                }
            }
            result.permissionInfo = [sub_startDate:sub_startDate,sub_endDate:sub_endDate,sub_name:original_sub?.name?:'',sub_id:original_sub?.id?:'', sub_license: original_sub?.owner?.reference?:'']


            log.debug("Worksheet upload on behalf of ${org_name}, ${org_id}, ${org_shortcode}");

            def sub_info = []
            // Step 2 - Row 5 (6, but 0 based) contains package identifiers starting in column 4(5)
            Row package_ids_row = firstSheet.getRow(5)
            for (int i = SO_START_COL; ((i < package_ids_row.getLastCellNum()) && (package_ids_row.getCell(i))); i++) {
                log.debug("Got package identifier: ${package_ids_row.getCell(i).toString()}");
                def sub_id = package_ids_row.getCell(i).toString()
                def sub_rec = genericOIDService.resolveOID(sub_id) // Subscription.get(sub_id);
                if (sub_rec) {
                    sub_info.add(sub_rec);
                } else {
                    log.error("Unable to resolve the package identifier in row 6 column ${i + 5}, please check");
                    return
                }
            }

            result.entitlements = []

            boolean processing = true
            // Step three, process each title row, starting at row 11(10)
            for (int i = SO_START_ROW; ((i < firstSheet.getLastRowNum()) && (processing)); i++) {
                // log.debug("processing row ${i}");

                Row title_row = firstSheet.getRow(i)
                // Title ID
                def title_id = title_row.getCell(0).toString()
                if (title_id == 'END') {
                    // log.debug("Encountered END title");
                    processing = false;
                } else {
                    // log.debug("Upload Process title: ${title_id}, num subs=${sub_info.size()}, last cell=${title_row.getLastCellNum()}");
                    def title_id_long = Long.parseLong(title_id)
                    def title_rec = TitleInstance.get(title_id_long);
                    for (int j = 0; (((j + SO_START_COL) < title_row.getLastCellNum()) && (j <= sub_info.size())); j++) {
                        def resp_cell = title_row.getCell(j + SO_START_COL)
                        if (resp_cell) {
                            // log.debug("  -> Testing col[${j+SO_START_COL}] val=${resp_cell.toString()}");

                            def subscribe = resp_cell.toString()

                            // log.debug("Entry : sub:${subscribe}");

                            if (subscribe == 'Y' || subscribe == 'y') {
                                // log.debug("Add an issue entitlement from subscription[${j}] for title ${title_id_long}");

                                def entitlement_info = [:]
                                entitlement_info.base_entitlement = extractEntitlement(sub_info[j], title_id_long)
                                if (entitlement_info.base_entitlement) {
                                    entitlement_info.title_id = title_id_long
                                    entitlement_info.subscribe = subscribe

                                    entitlement_info.start_date = title_row.getCell(4)
                                    entitlement_info.end_date = title_row.getCell(5)
                                    entitlement_info.coverage = title_row.getCell(6)
                                    entitlement_info.coverage_note = title_row.getCell(7)
                                    entitlement_info.core_status = title_row.getCell(10) // Moved from 8


                                    // log.debug("Added entitlement_info ${entitlement_info}");
                                    result.entitlements.add(entitlement_info)
                                } else {
                                    log.error("TIPP not found in package.");
                                    flash.error = message(code:'myinst.processRenewalUpload.error.tipp', args:[title_id_long]);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.error("Input stream is null");
        }
        log.debug("Done");

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def extractEntitlement(pkg, title_id) {
        def result = pkg.tipps.find { e -> e.title?.id == title_id }
        if (result == null) {
            log.error("Failed to look up title ${title_id} in package ${pkg.name}");
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processRenewal() {
        log.debug("-> renewalsUpload params: ${params}");
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${result.institution.name}. Please request access on the profile page");
            return;
        }

        log.debug("entitlements...[${params.ecount}]");

        int ent_count = Integer.parseInt(params.ecount);
        Calendar now = Calendar.getInstance();

        def sub_startDate = params.subscription?.copyStart ? parseDate(params.subscription?.start_date,possible_date_formats) : null
        def sub_endDate = params.subscription?.copyEnd ? parseDate(params.subscription?.end_date,possible_date_formats): null
        def copy_documents = params.subscription?.copy_docs && params.subscription.copyDocs
        def old_subOID = params.subscription.copy_docs
        def old_subname = "${params.subscription.name} ${now.get(Calendar.YEAR)+1}"

        def new_subscription = new Subscription(
                identifier: java.util.UUID.randomUUID().toString(),
                status: RefdataCategory.lookupOrCreate(RDConstants.SUBSCRIPTION_STATUS, 'Under Consideration'),
                impId: java.util.UUID.randomUUID().toString(),
                name: old_subname ?: "Unset: Generated by import",
                startDate: sub_startDate,
                endDate: sub_endDate,
                //previousSubscription: old_subOID ?: null, previousSubscription oberhauled by Links table entry as of ERMS-800 (ERMS-892) et al.
                type: Subscription.get(old_subOID)?.type ?: null,
                isPublic: RefdataValue.getByValueAndCategory('No', RDConstants.Y_N),
                owner: params.subscription.copyLicense ? (Subscription.get(old_subOID)?.owner) : null,
                resource: Subscription.get(old_subOID)?.resource ?: null,
                form: Subscription.get(old_subOID)?.form ?: null
        )
        log.debug("New Sub: ${new_subscription.startDate}  - ${new_subscription.endDate}")
        def packages_referenced = []
        Date earliest_start_date = null
        Date latest_end_date = null

        if (new_subscription.save()) {
            // assert an org-role
            def org_link = new OrgRole(org: result.institution,
                    sub: new_subscription,
                    roleType: RDStore.OR_SUBSCRIBER
            ).save();

            // as of ERMS-892: set up new previous/next linking
            Links prevLink = new Links(source:new_subscription.id,destination:old_subOID,objectType:Subscription.class.name,linkType:RDStore.LINKTYPE_FOLLOWS,owner:contextService.org)
            if(old_subOID)
                prevLink.save()
            else log.error("Problem linking new subscription, ${prevLink.errors}")
        } else {
            log.error("Problem saving new subscription, ${new_subscription.errors}");
        }

        new_subscription.save(flush: true);
        if(copy_documents){
            String subOID =  params.subscription.copy_docs
            def sourceOID = "${new_subscription.getClass().getName()}:${subOID}"
            docstoreService.copyDocuments(sourceOID,"${new_subscription.getClass().getName()}:${new_subscription.id}")
        }

        if (!new_subscription.issueEntitlements) {
           // new_subscription.issueEntitlements = new java.util.TreeSet()
        }

        if(ent_count > -1){
        for (int i = 0; i <= ent_count; i++) {
            def entitlement = params.entitlements."${i}";
            log.debug("process entitlement[${i}]: ${entitlement} - TIPP id is ${entitlement.tipp_id}");

            def dbtipp = TitleInstancePackagePlatform.get(entitlement.tipp_id)

            if (!packages_referenced.contains(dbtipp.pkg)) {
                packages_referenced.add(dbtipp.pkg)
                def new_package_link = new SubscriptionPackage(subscription: new_subscription, pkg: dbtipp.pkg).save();
                if ((earliest_start_date == null) || (dbtipp.pkg.startDate < earliest_start_date))
                    earliest_start_date = dbtipp.pkg.startDate
                if ((latest_end_date == null) || (dbtipp.pkg.endDate > latest_end_date))
                    latest_end_date = dbtipp.pkg.endDate
            }

            if (dbtipp) {
                def live_issue_entitlement = RDStore.TIPP_STATUS_CURRENT
                def is_core = false

                def new_core_status = null;

                switch (entitlement.core_status?.toUpperCase()) {
                    case 'Y':
                    case 'YES':
                        new_core_status = RefdataCategory.lookupOrCreate(RDConstants.CORE_STATUS, 'Yes');
                        is_core = true;
                        break;
                    case 'P':
                    case 'PRINT':
                        new_core_status = RefdataCategory.lookupOrCreate(RDConstants.CORE_STATUS, 'Print');
                        is_core = true;
                        break;
                    case 'E':
                    case 'ELECTRONIC':
                        new_core_status = RefdataCategory.lookupOrCreate(RDConstants.CORE_STATUS, 'Electronic');
                        is_core = true;
                        break;
                    case 'P+E':
                    case 'E+P':
                    case 'PRINT+ELECTRONIC':
                    case 'ELECTRONIC+PRINT':
                        new_core_status = RefdataCategory.lookupOrCreate(RDConstants.CORE_STATUS, 'Print+Electronic');
                        is_core = true;
                        break;
                    default:
                        new_core_status = RefdataCategory.lookupOrCreate(RDConstants.CORE_STATUS, 'No');
                        break;
                }

                def new_start_date = entitlement.start_date ? parseDate(entitlement.start_date, possible_date_formats) : null
                def new_end_date = entitlement.end_date ? parseDate(entitlement.end_date, possible_date_formats) : null


                // entitlement.is_core
                def new_ie = new IssueEntitlement(subscription: new_subscription,
                        status: live_issue_entitlement,
                        tipp: dbtipp,
                        startDate: new_start_date ?: dbtipp.startDate,
                        startVolume: dbtipp.startVolume,
                        startIssue: dbtipp.startIssue,
                        endDate: new_end_date ?: dbtipp.endDate,
                        endVolume: dbtipp.endVolume,
                        endIssue: dbtipp.endIssue,
                        embargo: dbtipp.embargo,
                        coverageDepth: dbtipp.coverageDepth,
                        coverageNote: dbtipp.coverageNote,
                        coreStatus: new_core_status
                )

                if (new_ie.save()) {
                    log.debug("new ie saved");
                } else {
                    new_ie.errors.each { e ->
                        log.error("Problem saving new ie : ${e}");
                    }
                }
            } else {
                log.debug("Unable to locate tipp with id ${entitlement.tipp_id}");
            }
        }
        }
        log.debug("done entitlements...");

        new_subscription.startDate = sub_startDate ?: earliest_start_date
        new_subscription.endDate = sub_endDate ?: latest_end_date
        new_subscription.save()

        if (new_subscription)
            redirect controller: 'subscription', action: 'index', id: new_subscription.id
        else
            redirect action: 'renewalsUpload', params: params
    }

    def addCellComment(row, cell, comment_text, drawing, factory) {

        // When the comment box is visible, have it show in a 1x3 space
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 9);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 11);

        // Create the comment and set the text+author
        def comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(comment_text);
        comment.setString(str);
        comment.setAuthor("LAS:eR System");

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }