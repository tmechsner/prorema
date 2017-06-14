this.getClass().classLoader.rootLoader.addURL(new File("h2-latest.jar").toURL());
this.getClass().classLoader.rootLoader.addURL(new File("mysql-connector-java-5.1.38-bin.jar").toURL());

import groovy.sql.Sql

def h2Url='jdbc:h2:file:/Users/timo/workspace-uni/tdpe/ss16team4-1/testdb'
def h2User='sa'
def h2Passwd=''

def mysqlUrl='jdbc:mysql://192.168.99.100:32769/prorema2'
def mysqlUser='root'
def mysqlPasswd='root'
def mysqlDatabase='prorema'


sql = Sql.newInstance(h2Url, h2User, h2Passwd, 'org.h2.Driver' )

def tables = [:]

sql.eachRow("select * from information_schema.columns where table_schema='PUBLIC'") {
    println("row");
    if(!it.TABLE_NAME.endsWith("_MY")) {
        if (tables[it.TABLE_NAME] == null) {
            tables[it.TABLE_NAME] = []
        }
        tables[it.TABLE_NAME] += it.COLUMN_NAME;
    }
}

tables.each{tab, cols ->
    println("processing $tab")
    println("droppin $tab"+"_my")

    sql.execute("DROP TABLE IF EXISTS "+tab+"_my;")
    sql.execute("create linked table "+tab+"_my ('com.mysql.jdbc.Driver', '"+mysqlUrl+"', '"+mysqlUser+"', '"+mysqlPasswd+"', '"+mysqlDatabase+"."+tab.toLowerCase()+"');")

    sql.eachRow("select count(*) as c from " + tab + "_my"){println("deleting $it.c entries from mysql table")}
    result = sql.execute("delete from "+tab+"_my")
    colString = cols.join(", ")
    sql.eachRow("select count(*) as c from " + tab){println("starting to copy $it.c entries")}
    sql.execute("insert into " + tab + "_my ("+colString+") select "+colString+" from " + tab)
}