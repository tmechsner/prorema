/**
 * Created by Philip on 20.06.16.
 */


/*<![CDATA[*/
// Highlight the actual sector in the "bereiche-Reiter" (sends class="active" to nav-tabs)

window.onload = function () {
    var action2 = window.location.search;
    if (action2.indexOf("monthly")>=0){
            action2 = action2.substring(0, action2.length - 13); //13= length of "?monthly=true"
        }
    if (action2.indexOf("fromDate") >= 0){
        action2 = action2.substring(0, action2.length - 38); //38= length of "?fromDate=xxxx-xx-xx"&toDate=xxxx-xx-xx"
    }
    $('ul.nav a[href="/schedule' + action2 + '"]').parent().addClass('active');
    if (action2 != "") {
        var action3 = window.location.search;
            if (action3 !="monthly=true"){
            $("#start").removeClass("active");
        }
    }

};


$(document).ready(function () {

    // "Wochentakt" "Monatstakt" URL generator
    var weekview = document.getElementById("weekview");
    var monthview = document.getElementById("monthview");
    var path = document.URL;
    if (path.indexOf("monthly=true") >= 0) {
    monthview.setAttribute("href", path);
    monthview.setAttribute("class", "btn btn-success active");

    weekview.setAttribute("class", "btn btn-default");

        if (path.indexOf("?monthly=true&") >= 0) {
        var weekviewlink = path.replace('?monthly=true&', '?');
        }
        else if (path.indexOf("&monthly=true") >= 0){
        var weekviewlink = path.replace('&monthly=true', '');
        }
        else{
        var weekviewlink = path.replace('?monthly=true', '');
        }

    weekview.setAttribute("href",weekviewlink);
    }
    else{
        if (path.indexOf("schedule?") >= 0){
        monthview.setAttribute("href", path+ "&monthly=true");
        }
        else{
        monthview.setAttribute("href", path+ "?monthly=true");
        }
    weekview.setAttribute("href",path);
    }



    // Generator for actual Dates of the Timefilter-Buttons
    var c = new Date();
    var d = new Date();
    var e = new Date();
    var g = new Date();
    var h = new Date();
    var i = new Date();
    var j = new Date();
    var k = new Date();
    var prevMonthLink = document.getElementById("prevMonthLink");
    var nextMonthLink = document.getElementById("nextMonthLink");
    var thisYearLink = document.getElementById("thisYearLink");
    var actualMonthLink = document.getElementById("actualMonthLink");
    var threemonthsLink = document.getElementById("3monthsLink");
    var sixmonthslink = document.getElementById("6monthslink");
    c.setDate(c.getDate() - 30);
    e.setDate(e.getDate() + 30); //end Date 1 - adding 30 days from today
    g.setDate(g.getDate() + 60); //end Date 2 - adding 60 days from today
    h.setDate(h.getDate() + 365); // 1 Year
    i.setDate(h.getDate() + 92); // 3 Months
    j.setDate(h.getDate() + 183); // 6 Months
    k.setDate(h.getDate() + 365);

    var date1 = c.toISOString().slice(0, 10);
    var date = d.toISOString().slice(0, 10);
    var date2 = e.toISOString().slice(0, 10);
    var date4 = g.toISOString().slice(0, 10);
    var date6 = h.toISOString().slice(0, 10);
    var date7 = i.toISOString().slice(0, 10);
    var date8 = j.toISOString().slice(0, 10);

    var pathArray = document.URL;
    if (pathArray.indexOf("monthly") >= 0){
    var monthlyvar = "&monthly=true";
    }
    else{
    var monthlyvar ="";
    }

    var pathArray = pathArray.replace('?monthly=true', '');
    var pathArray = pathArray.replace('&monthly=true', '');

    if (pathArray.indexOf("?") >= 0) {

        if (pathArray.indexOf("fromDate") >= 0) {

            //find  FromDate (actual shown timemark) and create new time filters depending on the given fromdate
            var n = pathArray.indexOf("fromDate");
            var actualdateStr = pathArray.substring(n+9, n+19);

                        var yr1   = parseInt(actualdateStr.substring(0,4));
                        var mon1  = parseInt(actualdateStr.substring(5,7));
                        var dt1   = parseInt(actualdateStr.substring(8,10));
                        var actualdate = new Date(yr1, mon1-1, dt1);

            //var actualdate = new Date (actualdateStr);
            var myDate1=new Date(actualdate);
            var myDate2=new Date(actualdate);
            var myDate0=new Date(actualdate);
            var myDate=new Date(actualdate);
            myDate.setDate(actualdate.getDate());
            myDate0.setDate(actualdate.getDate() - 30);
            myDate1.setDate(actualdate.getDate() + 30);
            myDate2.setDate(actualdate.getDate() + 60);

            var qdate = myDate.toISOString().slice(0, 10);
            var qdate2 = myDate1.toISOString().slice(0, 10);
            var qdate4 = myDate2.toISOString().slice(0, 10);
            var qdate0 = myDate0.toISOString().slice(0, 10);

            pathArray = pathArray.substring(0, pathArray.length - 38);

            if (pathArray.indexOf("unitId") >= 0 || pathArray.indexOf("emId") >= 0  || pathArray.indexOf("projectId") >= 0 ) {
                // time filters when other filer are set
                prevMonthLink.setAttribute("href", pathArray + "&fromDate=" + qdate0 + "&toDate=" + qdate + monthlyvar);
                nextMonthLink.setAttribute("href", pathArray + "&fromDate=" + qdate2 + "&toDate=" + qdate4 + monthlyvar);
                thisYearLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date6 + '&monthly=true');
                actualMonthLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date2 + monthlyvar);
                threemonthsLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date7 + monthlyvar);
                sixmonthslink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date8 + monthlyvar);
            }
            else {
                //time filters when no other filter is set
                prevMonthLink.setAttribute("href", pathArray + "?fromDate=" + qdate0 + "&toDate=" + qdate + monthlyvar);
                nextMonthLink.setAttribute("href", pathArray + "?fromDate=" + qdate2 + "&toDate=" + qdate4 + monthlyvar);
                thisYearLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date6 + '&monthly=true');
                actualMonthLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date2 + monthlyvar);
                threemonthsLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date7 + monthlyvar);
                sixmonthslink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date8 + monthlyvar);
            }
        }
        else {
            //time fiters generated for fromDate=today (fromDate = empty) and exciting other filters
            prevMonthLink.setAttribute("href", pathArray + "&fromDate=" + date1 + "&toDate=" + date + monthlyvar);
            nextMonthLink.setAttribute("href", pathArray + "&fromDate=" + date2 + "&toDate=" + date4 + monthlyvar);
            thisYearLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date6 + '&monthly=true');
            actualMonthLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date2 + monthlyvar);
            threemonthsLink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date7 + monthlyvar);
            sixmonthslink.setAttribute("href", pathArray + "&fromDate=" + date + "&toDate=" + date8 + monthlyvar);
        }
    }
    else {
            //time fiters generated for fromDate=today (fromDate = empty) and no other filter set (except monthly/weekly-set)
        prevMonthLink.setAttribute("href", pathArray + "?fromDate=" + date1 + "&toDate=" + date + monthlyvar);
        nextMonthLink.setAttribute("href", pathArray + "?fromDate=" + date2 + "&toDate=" + date4 + monthlyvar);
        thisYearLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date6 + '&monthly=true');
        actualMonthLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date2 + monthlyvar);
        threemonthsLink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date7 + monthlyvar);
        sixmonthslink.setAttribute("href", pathArray + "?fromDate=" + date + "&toDate=" + date8 + monthlyvar);
    }


    //Pickaday for the custom date range

    var picker = new Pikaday({
        field: document.getElementById('datepicker'),
        format: 'YYYY-MM-DD',
    });

    var picker2 = new Pikaday({
        field: document.getElementById('datepicker2'),
        format: 'YYYY-MM-DD',
    });

    var monthlyvar2 = ""; // no monthly=true by default

    document.getElementById("week").onclick = function () {
            monthlyvar2 = "";
            week.setAttribute("class","btn btn-success btn-sm active");
            month.setAttribute("class", "btn btn-default btn-sm");
        };

    document.getElementById("month").onclick = function () {
            monthlyvar2 = "&monthly=true";
            month.setAttribute("class","btn btn-success btn-sm active");
            week.setAttribute("class", "btn btn-default btn-sm");
        };




        document.getElementById("updateme").onclick = function () {
        var customDateLink = document.getElementById("customDateLink");

        if (picker == '' || picker2 == ''){
        alert("Bitte wählen Sie ein Anfangs- und Enddatum aus und klicken dann nochmal 'Auswählen'.");
        }
        else{
        customDateLink.setAttribute("class", "btn btn-success"); //Makes "Anwenden"-Button clickable
        }


        if (pathArray.indexOf("?") >= 0) {
            customDateLink.setAttribute("href", pathArray + "&fromDate=" + picker + "&toDate=" + picker2 + monthlyvar2);

        } else {
            customDateLink.setAttribute("href", pathArray + "?fromDate=" + picker + "&toDate=" + picker2 + monthlyvar2);
        }

    }
    // copy buttons to top page
    var element = document.getElementById('origin');
    var copy = element.cloneNode(true);
    var copybuttons = document.getElementById('copybuttons');
    copybuttons.appendChild(copy);

     });

             // Save actual schedule view (date, person, unit) as cookie


                                function setCookie(cname) {
                                    cvalue = document.URL;
                                    //cookies should not expire, but need a exp-date => year 9999
                                    document.cookie = cname + "=" + cvalue + "; expires=Fri, 31 Dec 9999 23:59:59 GMT";
                                    alert("Die aktuelle Ansicht wurde in Filter "+cname+" erfolgreich gespeichert.");
                                }

                                function getCookie(cname) {
                                    var name = cname + "=";
                                    var ca = document.cookie.split(';');
                                    for(var i = 0; i <ca.length; i++) {
                                        var c = ca[i];
                                        while (c.charAt(0)==' ') {
                                            c = c.substring(1);
                                        }
                                        if (c.indexOf(name) == 0) {
                                            return c.substring(name.length,c.length);
                                        }
                                    }
                                    return "";
                                }

                                // load browser cookies into schedule

                          $(document).ready(function(){

                                    var save1 = document.getElementById("save1");
                                    var c1=getCookie(1);
                                    if (c1!="") {
                                      save1.setAttribute("href",c1);
                                      save1.setAttribute("class","btn btn-success btn-xs");
                                    }

                                     var save2 = document.getElementById("save2");
                                     var c2=getCookie(2);
                                     if (c2!="") {
                                       save2.setAttribute("href",c2);
                                       save2.setAttribute("class","btn btn-success btn-xs");
                                     }

                                     var save3 = document.getElementById("save3");
                                     var c3=getCookie(3);
                                     if (c3!="") {
                                       save3.setAttribute("href",c3);
                                       save3.setAttribute("class","btn btn-success btn-xs");
                                     }

                            })
              /*]]>*/


$(function () {
    $('[data-toggle="tooltip"]').tooltip()
});



