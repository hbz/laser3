-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>

-- 2019-12-19
-- ERMS-1992: translations provided for subscription custom properties
-- changesets in changelog-2019-12-20.groovy
-- update property_definition set pd_name = 'GASCO display name' where pd_name = 'GASCO-Anzeigename' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'GASCO negotiator name' where pd_name = 'GASCO-Verhandlername' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'GASCO information link' where pd_name = 'GASCO-Information-Link' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'EZB tagging (yellow)' where pd_name = 'EZB Gelbschaltung' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Pricing advantage by licensing of another product' where pd_name = 'Preisvorteil durch weitere Produktteilnahme' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Product dependency' where pd_name = 'Produktabhängigkeit' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Open country-wide' where pd_name = 'Bundesweit offen' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Billing done by provider' where pd_name = 'Rechnungsstellung durch Anbieter' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Due date for volume discount' where pd_name = 'Mengenrabatt Stichtag' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Time span for testing' where pd_name = 'Testzeitraum' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Joining during the period' where pd_name = 'Unterjähriger Einstieg' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Newcomer discount' where pd_name = 'Neueinsteigerrabatt' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Time of billing' where pd_name = 'Rechnungszeitpunkt' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Payment target' where pd_name = 'Zahlungsziel' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Price rounded' where pd_name = 'Preis gerundet' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Partial payment' where pd_name = 'Teilzahlung' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Statistic' where pd_name = 'Statistik' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Statistic access' where pd_name = 'Statistikzugang' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Statistics Link' where pd_name = 'StatisticsLink' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Admin Access' where pd_name = 'AdminAccess' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Admin Link' where pd_name = 'AdminLink' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Private institutions' where pd_name = 'Private Einrichtungen' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Perennial term' where pd_name = 'Mehrjahreslaufzeit' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Perennial term checked' where pd_name = 'Mehrjahreslaufzeit ausgewählt' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Discount' where pd_name = 'Rabatt' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Scale of discount' where pd_name = 'Rabattstaffel' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Calculation of discount' where pd_name = 'Rabatt Zählung' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Term of notice' where pd_name = 'Kündigungsfrist' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Additional software necessary?' where pd_name = 'Zusätzliche Software erforderlich?' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Price increase' where pd_name = 'Preissteigerung' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Price depending on' where pd_name = 'Preis abhängig von' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Cancellation rate' where pd_name = 'Abbestellquote' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Order number in purchasing system' where pd_name = 'Bestellnummer im Erwerbungssystem' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Credentials for users (per journal)' where pd_name = 'Zugangskennungen für Nutzer (pro Zeitschrift)' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Tax exemption' where pd_name = 'TaxExemption' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Subscription number of editor' where pd_name = 'Subscriptionsnummer vom Verlag' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Subscription number of provider' where pd_name = 'Subskriptionsnummer des Lieferanten' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'DBIS entry' where pd_name = 'DBIS-Eintrag' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'DBIS link' where pd_name = 'DBIS-Link' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Cancellation reason' where pd_name = 'Abbestellgrund' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Hosting fee' where pd_name = 'Hosting-Gebühr' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Pick&Choose package' where pd_name = 'Pick&Choose-Paket' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'PDA/EBS model' where pd_name = 'PDA/EBS-Programm' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Specialised statistics / classification' where pd_name = 'Fachstatistik / Klassifikation' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Perpetual access' where pd_name = 'Archivzugriff' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'Restricted user group' where pd_name = 'Eingeschränkter Benutzerkreis' and pd_description ='Subscription Property';
-- update property_definition set pd_name = 'SFX entry' where pd_name = 'SFX-Eintrag' and pd_description ='Subscription Property';

-- 2020-01-17
-- ERMS-2038: migrate refdata translations
-- changesets in changelog-2020-01-17.groovy

alter table refdata_category add column rdc_description_de varchar(255);
alter table refdata_category add column rdc_description_en varchar(255);

update refdata_category
set rdc_description_de = i10n_value_de, rdc_description_en = i10n_value_en
from i10n_translation
where rdc_id = i10n_reference_id and i10n_reference_class = 'com.k_int.kbplus.RefdataCategory' and i10n_reference_field = 'desc';

delete from i10n_translation
where i10n_reference_class like 'com.k_int.kbplus.RefdataCategory%' and i10n_reference_field = 'desc';

alter table refdata_value add column rdv_value_de varchar(255);
alter table refdata_value add column rdv_value_en varchar(255);

update refdata_value
set rdv_value_de = i10n_value_de, rdv_value_en = i10n_value_en
from i10n_translation
where rdv_id = i10n_reference_id and i10n_reference_class = 'com.k_int.kbplus.RefdataValue' and i10n_reference_field = 'value';

delete from i10n_translation
where i10n_reference_class like 'com.k_int.kbplus.RefdataValue%' and i10n_reference_field = 'value';


-- 2020-01-20
-- ERMS-2072: migrate refdata translations
-- changesets in changelog-2020-01-17.groovy

update refdata_category set rdc_description = 'access.choice.remote' where rdc_description = 'Access choice remote';
update refdata_category set rdc_description = 'access.method' where rdc_description = 'Access Method';
update refdata_category set rdc_description = 'access.method.ip' where rdc_description = 'Access Method IP';
update refdata_category set rdc_description = 'access.point.type' where rdc_description = 'Access Point Type';
update refdata_category set rdc_description = 'address.type' where rdc_description = 'AddressType';
update refdata_category set rdc_description = 'authority' where rdc_description = 'Authority';
update refdata_category set rdc_description = 'category.a.f' where rdc_description = 'Category A-F';
update refdata_category set rdc_description = 'cluster.role' where rdc_description = 'Cluster Role';
update refdata_category set rdc_description = 'cluster.type' where rdc_description = 'ClusterType';
update refdata_category set rdc_description = 'combo.status' where rdc_description = 'Combo Status';
update refdata_category set rdc_description = 'combo.type' where rdc_description = 'Combo Type';
update refdata_category set rdc_description = 'concurrent.access' where rdc_description = 'ConcurrentAccess';
update refdata_category set rdc_description = 'confidentiality' where rdc_description = 'Confidentiality';
update refdata_category set rdc_description = 'contact.content.type' where rdc_description = 'ContactContentType';
update refdata_category set rdc_description = 'contact.type' where rdc_description = 'ContactType';
update refdata_category set rdc_description = 'core.status' where rdc_description = 'CoreStatus';
update refdata_category set rdc_description = 'cost.configuration' where rdc_description = 'Cost configuration';
update refdata_category set rdc_description = 'cost.item.type' where rdc_description = 'CostItem.Type';
update refdata_category set rdc_description = 'cost.item.category' where rdc_description = 'CostItemCategory';
update refdata_category set rdc_description = 'cost.item.element' where rdc_description = 'CostItemElement';
update refdata_category set rdc_description = 'cost.item.status' where rdc_description = 'CostItemStatus';
update refdata_category set rdc_description = 'country' where rdc_description = 'Country';
update refdata_category set rdc_description = 'creator.type' where rdc_description = 'CreatorType';
update refdata_category set rdc_description = 'currency' where rdc_description = 'Currency';
update refdata_category set rdc_description = 'customer.identifier.type' where rdc_description = 'CustomerIdentifier.Type';
update refdata_category set rdc_description = 'document.context.status' where rdc_description = 'Document Context Status';
update refdata_category set rdc_description = 'document.type' where rdc_description = 'Document Type';
update refdata_category set rdc_description = 'existence' where rdc_description = 'Existence';
update refdata_category set rdc_description = 'fact.metric' where rdc_description = 'FactMetric';
update refdata_category set rdc_description = 'fact.type' where rdc_description = 'FactType';
update refdata_category set rdc_description = 'federal.state' where rdc_description = 'Federal State';
update refdata_category set rdc_description = 'funder.type' where rdc_description = 'Funder Type';
update refdata_category set rdc_description = 'gender' where rdc_description = 'Gender';
update refdata_category set rdc_description = 'ie.accept.status' where rdc_description = 'IE Accept Status';
update refdata_category set rdc_description = 'ie.access.status' where rdc_description = 'IE Access Status';
update refdata_category set rdc_description = 'ie.medium' where rdc_description = 'IEMedium';
update refdata_category set rdc_description = 'ill.code' where rdc_description = 'Ill code';
update refdata_category set rdc_description = 'indemnification' where rdc_description = 'Indemnification';
update refdata_category set rdc_description = 'invoicing' where rdc_description = 'Invoicing';
update refdata_category set rdc_description = 'ipv4.address.format' where rdc_description = 'IPv4 Address Format';
update refdata_category set rdc_description = 'ipv6.address.format' where rdc_description = 'IPv6 Address Format';
update refdata_category set rdc_description = 'language' where rdc_description = 'Language';
update refdata_category set rdc_description = 'library.network' where rdc_description = 'Library Network';
update refdata_category set rdc_description = 'license.category' where rdc_description = 'LicenseCategory';
update refdata_category set rdc_description = 'license.remote.access' where rdc_description = 'Lincense.RemoteAccess';
update refdata_category set rdc_description = 'library.type' where rdc_description = 'Library Type';
update refdata_category set rdc_description = 'license.status' where rdc_description = 'License Status';
update refdata_category set rdc_description = 'license.type' where rdc_description = 'License Type';
update refdata_category set rdc_description = 'license.arc.archival.copy.content' where rdc_description = 'License.Arc.ArchivalCopyContent';
update refdata_category set rdc_description = 'license.arc.archival.copy.cost' where rdc_description = 'License.Arc.ArchivalCopyCost';
update refdata_category set rdc_description = 'license.arc.archival.copy.time' where rdc_description = 'License.Arc.ArchivalCopyTime';
update refdata_category set rdc_description = 'license.arc.archival.copy.transmission.format' where rdc_description = 'License.Arc.ArchivalCopyTransmissionFormat';
update refdata_category set rdc_description = 'license.arc.authorized' where rdc_description = 'License.Arc.Authorized';
update refdata_category set rdc_description = 'license.arc.hosting.restriction' where rdc_description = 'License.Arc.HostingRestriction';
update refdata_category set rdc_description = 'license.arc.hosting.solution' where rdc_description = 'License.Arc.HostingSolution';
update refdata_category set rdc_description = 'license.arc.hosting.time' where rdc_description = 'License.Arc.HostingTime';
update refdata_category set rdc_description = 'license.arc.payment.note' where rdc_description = 'License.Arc.PaymentNote';
update refdata_category set rdc_description = 'license.arc.title.transfer.regulation' where rdc_description = 'License.Arc.TitletransferRegulation';
update refdata_category set rdc_description = 'license.arc.title.transfer.regulation' where rdc_description = 'License.Arc.TitleTransferRegulation';
update refdata_category set rdc_description = 'license.oa.corresponding.author.identification' where rdc_description = 'License.OA.CorrespondingAuthorIdentification';
update refdata_category set rdc_description = 'license.oa.earc.version' where rdc_description = 'License.OA.eArcVersion';
update refdata_category set rdc_description = 'license.oa.license.to.publish' where rdc_description = 'License.OA.LicenseToPublish';
update refdata_category set rdc_description = 'license.oa.receiving.modalities' where rdc_description = 'License.OA.ReceivingModalities';
update refdata_category set rdc_description = 'license.oa.repository' where rdc_description = 'License.OA.Repository';
update refdata_category set rdc_description = 'license.oa.type' where rdc_description = 'License.OA.Type';
update refdata_category set rdc_description = 'license.remote.access2' where rdc_description = 'License.RemoteAccess';
update refdata_category set rdc_description = 'license.statistics.delivery' where rdc_description = 'License.Statistics.Delivery';
update refdata_category set rdc_description = 'license.statistics.format' where rdc_description = 'License.Statistics.Format';
update refdata_category set rdc_description = 'license.statistics.frequency' where rdc_description = 'License.Statistics.Frequency';
update refdata_category set rdc_description = 'license.statistics.standards' where rdc_description = 'License.Statistics.Standards';
update refdata_category set rdc_description = 'license.statistics.user.creds' where rdc_description = 'License.Statistics.UserCreds';
update refdata_category set rdc_description = 'link.type' where rdc_description = 'Link Type';
update refdata_category set rdc_description = 'mail.template.language' where rdc_description = 'MailTemplate Language';
update refdata_category set rdc_description = 'mail.template.type' where rdc_description = 'MailTemplate Type';
update refdata_category set rdc_description = 'number.type' where rdc_description = 'Number Type';
update refdata_category set rdc_description = 'organisational.role' where rdc_description = 'Organisational Role';
update refdata_category set rdc_description = 'org.sector' where rdc_description = 'OrgSector';
update refdata_category set rdc_description = 'org.status' where rdc_description = 'OrgStatus';
update refdata_category set rdc_description = 'org.type' where rdc_description = 'OrgRoleType';

update property_definition set pd_rdc = 'access.choice.remote' where pd_rdc = 'Access choice remote';
update property_definition set pd_rdc = 'access.method' where pd_rdc = 'Access Method';
update property_definition set pd_rdc = 'access.method.ip' where pd_rdc = 'Access Method IP';
update property_definition set pd_rdc = 'access.point.type' where pd_rdc = 'Access Point Type';
update property_definition set pd_rdc = 'address.type' where pd_rdc = 'AddressType';
update property_definition set pd_rdc = 'authority' where pd_rdc = 'Authority';
update property_definition set pd_rdc = 'category.a.f' where pd_rdc = 'Category A-F';
update property_definition set pd_rdc = 'cluster.role' where pd_rdc = 'Cluster Role';
update property_definition set pd_rdc = 'cluster.type' where pd_rdc = 'ClusterType';
update property_definition set pd_rdc = 'combo.status' where pd_rdc = 'Combo Status';
update property_definition set pd_rdc = 'combo.type' where pd_rdc = 'Combo Type';
update property_definition set pd_rdc = 'concurrent.access' where pd_rdc = 'ConcurrentAccess';
update property_definition set pd_rdc = 'confidentiality' where pd_rdc = 'Confidentiality';
update property_definition set pd_rdc = 'contact.content.type' where pd_rdc = 'ContactContentType';
update property_definition set pd_rdc = 'contact.type' where pd_rdc = 'ContactType';
update property_definition set pd_rdc = 'core.status' where pd_rdc = 'CoreStatus';
update property_definition set pd_rdc = 'cost.configuration' where pd_rdc = 'Cost configuration';
update property_definition set pd_rdc = 'cost.item.type' where pd_rdc = 'CostItem.Type';
update property_definition set pd_rdc = 'cost.item.category' where pd_rdc = 'CostItemCategory';
update property_definition set pd_rdc = 'cost.item.element' where pd_rdc = 'CostItemElement';
update property_definition set pd_rdc = 'cost.item.status' where pd_rdc = 'CostItemStatus';
update property_definition set pd_rdc = 'country' where pd_rdc = 'Country';
update property_definition set pd_rdc = 'creator.type' where pd_rdc = 'CreatorType';
update property_definition set pd_rdc = 'currency' where pd_rdc = 'Currency';
update property_definition set pd_rdc = 'customer.identifier.type' where pd_rdc = 'CustomerIdentifier.Type';
update property_definition set pd_rdc = 'document.context.status' where pd_rdc = 'Document Context Status';
update property_definition set pd_rdc = 'document.type' where pd_rdc = 'Document Type';
update property_definition set pd_rdc = 'existence' where pd_rdc = 'Existence';
update property_definition set pd_rdc = 'fact.metric' where pd_rdc = 'FactMetric';
update property_definition set pd_rdc = 'fact.type' where pd_rdc = 'FactType';
update property_definition set pd_rdc = 'federal.state' where pd_rdc = 'Federal State';
update property_definition set pd_rdc = 'funder.type' where pd_rdc = 'Funder Type';
update property_definition set pd_rdc = 'gender' where pd_rdc = 'Gender';
update property_definition set pd_rdc = 'ie.accept.status' where pd_rdc = 'IE Accept Status';
update property_definition set pd_rdc = 'ie.access.status' where pd_rdc = 'IE Access Status';
update property_definition set pd_rdc = 'ie.medium' where pd_rdc = 'IEMedium';
update property_definition set pd_rdc = 'ill.code' where pd_rdc = 'Ill code';
update property_definition set pd_rdc = 'indemnification' where pd_rdc = 'Indemnification';
update property_definition set pd_rdc = 'invoicing' where pd_rdc = 'Invoicing';
update property_definition set pd_rdc = 'ipv4.address.format' where pd_rdc = 'IPv4 Address Format';
update property_definition set pd_rdc = 'ipv6.address.format' where pd_rdc = 'IPv6 Address Format';
update property_definition set pd_rdc = 'language' where pd_rdc = 'Language';
update property_definition set pd_rdc = 'library.network' where pd_rdc = 'Library Network';
update property_definition set pd_rdc = 'license.category' where pd_rdc = 'LicenseCategory';
update property_definition set pd_rdc = 'license.remote.access' where pd_rdc = 'Lincense.RemoteAccess';
update property_definition set pd_rdc = 'library.type' where pd_rdc = 'Library Type';
update property_definition set pd_rdc = 'license.status' where pd_rdc = 'License Status';
update property_definition set pd_rdc = 'license.type' where pd_rdc = 'License Type';
update property_definition set pd_rdc = 'license.arc.archival.copy.content' where pd_rdc = 'License.Arc.ArchivalCopyContent';
update property_definition set pd_rdc = 'license.arc.archival.copy.cost' where pd_rdc = 'License.Arc.ArchivalCopyCost';
update property_definition set pd_rdc = 'license.arc.archival.copy.time' where pd_rdc = 'License.Arc.ArchivalCopyTime';
update property_definition set pd_rdc = 'license.arc.archival.copy.transmission.format' where pd_rdc = 'License.Arc.ArchivalCopyTransmissionFormat';
update property_definition set pd_rdc = 'license.arc.authorized' where pd_rdc = 'License.Arc.Authorized';
update property_definition set pd_rdc = 'license.arc.hosting.restriction' where pd_rdc = 'License.Arc.HostingRestriction';
update property_definition set pd_rdc = 'license.arc.hosting.solution' where pd_rdc = 'License.Arc.HostingSolution';
update property_definition set pd_rdc = 'license.arc.hosting.time' where pd_rdc = 'License.Arc.HostingTime';
update property_definition set pd_rdc = 'license.arc.payment.note' where pd_rdc = 'License.Arc.PaymentNote';
update property_definition set pd_rdc = 'license.arc.title.transfer.regulation' where pd_rdc = 'License.Arc.TitletransferRegulation';
update property_definition set pd_rdc = 'license.arc.title.transfer.regulation' where pd_rdc = 'License.Arc.TitleTransferRegulation';
update property_definition set pd_rdc = 'license.oa.corresponding.author.identification' where pd_rdc = 'License.OA.CorrespondingAuthorIdentification';
update property_definition set pd_rdc = 'license.oa.earc.version' where pd_rdc = 'License.OA.eArcVersion';
update property_definition set pd_rdc = 'license.oa.license.to.publish' where pd_rdc = 'License.OA.LicenseToPublish';
update property_definition set pd_rdc = 'license.oa.receiving.modalities' where pd_rdc = 'License.OA.ReceivingModalities';
update property_definition set pd_rdc = 'license.oa.repository' where pd_rdc = 'License.OA.Repository';
update property_definition set pd_rdc = 'license.oa.type' where pd_rdc = 'License.OA.Type';
update property_definition set pd_rdc = 'license.remote.access2' where pd_rdc = 'License.RemoteAccess';
update property_definition set pd_rdc = 'license.statistics.delivery' where pd_rdc = 'License.Statistics.Delivery';
update property_definition set pd_rdc = 'license.statistics.format' where pd_rdc = 'License.Statistics.Format';
update property_definition set pd_rdc = 'license.statistics.frequency' where pd_rdc = 'License.Statistics.Frequency';
update property_definition set pd_rdc = 'license.statistics.standards' where pd_rdc = 'License.Statistics.Standards';
update property_definition set pd_rdc = 'license.statistics.user.creds' where pd_rdc = 'License.Statistics.UserCreds';
update property_definition set pd_rdc = 'link.type' where pd_rdc = 'Link Type';
update property_definition set pd_rdc = 'mail.template.language' where pd_rdc = 'MailTemplate Language';
update property_definition set pd_rdc = 'mail.template.type' where pd_rdc = 'MailTemplate Type';
update property_definition set pd_rdc = 'number.type' where pd_rdc = 'Number Type';
update property_definition set pd_rdc = 'organisational.role' where pd_rdc = 'Organisational Role';
update property_definition set pd_rdc = 'org.sector' where pd_rdc = 'OrgSector';
update property_definition set pd_rdc = 'org.status' where pd_rdc = 'OrgStatus';
update property_definition set pd_rdc = 'org.type' where pd_rdc = 'OrgRoleType';








