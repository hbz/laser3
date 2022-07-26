databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1658828664522-1") {
        grailsChange {
            change {
                sql.execute('CREATE SEQUENCE counter4report_c4r_id_seq OWNED BY counter4report.c4r_id')
                sql.execute('CREATE SEQUENCE counter5report_c5r_id_seq OWNED BY counter5report.c5r_id')
                sql.execute('CREATE SEQUENCE laser_stats_cursor_lsc_id_seq OWNED BY laser_stats_cursor.lsc_id')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1658828664522-2") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE counter4report ALTER COLUMN c4r_id SET DEFAULT nextval('counter4report_c4r_id_seq'::regclass)")
                sql.execute("ALTER TABLE counter5report ALTER COLUMN c5r_id SET DEFAULT nextval('counter5report_c5r_id_seq'::regclass)")
                sql.execute("ALTER TABLE laser_stats_cursor ALTER COLUMN lsc_id SET DEFAULT nextval('laser_stats_cursor_lsc_id_seq'::regclass)")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1658828664522-3") {
        grailsChange {
            change {
                sql.execute("SELECT setval('counter4report_c4r_id_seq'::regclass, (SELECT max(c4r_id) FROM counter4report))")
                sql.execute("SELECT setval('counter5report_c5r_id_seq'::regclass, (SELECT max(c5r_id) FROM counter5report))")
                sql.execute("SELECT setval('laser_stats_cursor_lsc_id_seq'::regclass, (SELECT max(lsc_id) FROM laser_stats_cursor))")
            }
            rollback {}
        }
    }
}
