package de.unibielefeld.techfak.tdpe.prorema.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.domain.InformationPacker;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleElement;
import de.unibielefeld.techfak.tdpe.prorema.domain.planungsansicht.ScheduleRow;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.WorksOnService;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * PdfController to control the creation of the shedule-pdf.
 * Maps the pdf to /files/{file_name}.
 * How the pdf will be handeld, is determined by the browser intern handling.
 *
 * Fontsize is calculated to fit the interior of every cell in one line. (Except the weeks int the tableHead - 2 lines)
 */
@Controller
@Log4j2
public class PdfController extends ErrorHandler {

    private static byte[] source;

    private static ArrayList<String> weekDates;
    private static List<Employee> employees;
    private static List<ScheduleRow> rows;

    private static List<YearMonth> yearMonths;

    private static String[][] names;
    private static String[][] namesMonth;
    private static int[][] planned;
    private static int[][] plannedMonth;
    private static int[][] capacity;
    private static int[][] capacityMonth;

    private static int underbookedPrjs;
    private static int pipelinePrjsSize;
    private static int underbookedEmployees;
    private static int projectCount;

    private static String longestString;

    private boolean rotate;
    private boolean month;
    private boolean pdfCreated;

    private static float cellHeight;
    private static float cellWidth;

    private static float fontSize;
    private Font baseFont;
    private Font baseFontWithSize;

    public void update(List<YearMonth> yearMonths) {
        this.yearMonths = yearMonths;
    }

    public void update(ArrayList<String> weekDates, List<Employee> employees, List<ScheduleRow> rows) {
        this.employees = employees;
        this.weekDates = weekDates;
        this.rows = rows;
        updateLongestStringInTable();
        updateFontSize();
    }

    public void update(int underbookedPrjs, int pipelinePrjsSize, int underbookedEmployees, int projectCount) {
        this.underbookedPrjs = underbookedPrjs;
        this.pipelinePrjsSize = pipelinePrjsSize;
        this.underbookedEmployees = underbookedEmployees;
        this.projectCount = projectCount;
    }

    /**
     * Sets longestString to the longest String in the shedule table.
     */
    private void updateLongestStringInTable() {
        String max = "";

        for (String s : weekDates) {
            if (s.length() > max.length())
                max = s;
        }

        String s = ("In " + weekDates.size() + " Wochen");
        if (max.length() < s.length())
            max = s;
        String s2 = ("Fällt aus!");
        if (max.length() < s2.length())
            max = s2;

        for (Employee emp : employees) {
            String name = " " + emp.getNameTitle() + emp.getFirstName() + " " + emp.getLastName();
            if (name.length() > max.length())
                max = name;
        }
        longestString = max;
    }

    /**
     * Updates the Fontsize depending on the largest text in the shedule.
     * Used to determine fontsize, where every text will fit in one line, if the shedule size lets this happen.
     */
    private void updateFontSize() {

        try {
            updateLongestStringInTable();
            //Basic font
            BaseFont baseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

            //Maximum width of a String in the sheduleTable.
            float textWidth;
            if (longestString == null)
                textWidth = baseFont.getWidth("Mitarbeiter ");
            else textWidth = baseFont.getWidth(longestString + " ");

            //Calculate the cellWidth

            if (!month) {
                if (rotate)
                    cellWidth = (PageSize.A4.rotate().getWidth()) / (weekDates.size() + 1);
                else
                    cellWidth = PageSize.A4.getWidth() / (weekDates.size() + 1);
            }else if (month) {
                if (rotate)
                    cellWidth = (PageSize.A4.rotate().getWidth()) / (yearMonths.size()+1);
                else
                    cellWidth = PageSize.A4.getWidth() / (yearMonths.size()+1);
            }


            int rowCounter = 1; //not 0, because of the additional head row.
            for (Employee emp : employees) {
                rowCounter = rowCounter + emp.getInformationPacker().size();
            }

            cellHeight = (PageSize.A4.getHeight() / (rowCounter));
            if (rotate)
                cellHeight = (PageSize.A4.rotate().getHeight() / (rowCounter));

            //Set actual fontsize
            fontSize = 1000 * cellWidth / textWidth;

            this.baseFont= FontFactory.getFont(FontFactory.COURIER);
            baseFontWithSize = FontFactory.getFont(FontFactory.COURIER, fontSize);


        }catch (DocumentException | IOException ex) {
            log.error("Failed to create font while calculating fontsize.");
        }
    }

    /**
     * Fills a byte[] with the current shedule.
     * @param bytes byte[] to fill
     * @return filled byte[]
     */
    public byte[] fillPdf(byte[] bytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Document document;
            if (!rotate)
                document = new Document(PageSize.A4);
            else document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, bos);
            document.open();
            //Fill this table
            PdfPTable sheduleTable = new PdfPTable(weekDates.size()+1);

            fillTableHead(sheduleTable);

            fillTableBody(sheduleTable);

            Image image = turnToImg(sheduleTable,writer);
            if (weekDates.size()<=2) {
                try {
                    image.setAbsolutePosition(10,10);
                }catch (NullPointerException ex) {
                    log.error("Error while resizeing Pdf for 1 Month.");
                }
            }
            document.add(image);
            addLegendAndOverAllStats(document, writer);
            //addStatistiks(document, writer);

            document.close();

            bytes = bos.toByteArray();
            pdfCreated = true;
        } catch (DocumentException ex) {
            log.error("Can't write Document while creating pdf.");
            pdfCreated = false;
        }

        return bytes;
    }

    /**
     * Fills the head of the table.
     * Contains "Mitarbeiter" and the individual weeks.
     * @param sheduletable table to fill.
     */
    private void fillTableHead(PdfPTable sheduletable) {
        //add Mitarbeiter cell to table
        Phrase p = new Phrase(" Mitarbeiter", baseFontWithSize);
        PdfPCell cell = new PdfPCell(new Paragraph(p));
        cell.setMinimumHeight(cellHeight);
        sheduletable.addCell(cell);

        //add individual weeks to table
        for (int i = 0; i < weekDates.size(); i++) {
            String s = "In " + i + " Wochen \n";
            String d;
            try {
                d = weekDates.get(i);
            } catch (NullPointerException ex) {
                d = " ";
            }
            Phrase phrase = new Phrase(s+d, baseFontWithSize);
            PdfPCell cell2 = new PdfPCell(new Paragraph(phrase));
            cell2.setMinimumHeight(cellHeight);
            sheduletable.addCell(cell2);
        }
        sheduletable.completeRow();
    }

    /**
     * Fills the body of the table with the Mitarbeiter names and the projects they work on.
     * @param sheduletable table to fill.
     */
    private void fillTableBody(PdfPTable sheduletable) {

        List<String> wLoadwSchedule = getWorkloadAndWorkschedule();
        int wLwScounter = 0;

        for (Employee emp : employees) {
            String name;
            //beautification
            if (!emp.getNameTitle().equals(" "))
                name = " " + emp.getNameTitle() + emp.getFirstName() + " " + emp.getLastName();
            else name = emp.getNameTitle() + emp.getFirstName() + " " + emp.getLastName();

            //add name
            Phrase p = new Phrase(name, baseFontWithSize);
            PdfPCell nameCell = new PdfPCell(new Paragraph(p));
            nameCell.setRowspan(emp.getInformationPacker().size());
            sheduletable.addCell(nameCell);


            List<String> projectTexts = new LinkedList<>();
            String prjText = null;
            projectTexts.add("");
            //add projects
            for (int i = 0; i < emp.getInformationPacker().size(); i++){
                for (int j = 0; j < emp.getInformationPacker().get(i).size(); j++){
                    InformationPacker info = emp.getInformationPacker().get(i).get(j);

                    int colspan;
                    if (info.getWeekCount() < 4)
                        colspan = info.getWeekCount();
                    else colspan = 4;

                    if (info.getPrjName() != null) {
                        try {
                            //Compares the 'new' projectname to the last one. If they are equal, the new projectname will be null.
                            //If they are different, the 'new' one will be added to the list.
                            if (projectTexts.get(projectTexts.size()-1).equals(info.getPrjName())) {
                                prjText = null;
                            }
                            else {
                                prjText = info.getPrjName();
                                projectTexts.add(prjText);
                            }
                        } catch (IndexOutOfBoundsException ex) {
                            log.error( "Out of Bounds.");
                        }
                    } else {
                        prjText = null;
                    }

                    /*
                    if (prjText != null) {
                        try {
                        prjText = prjText + " " + wLoadwSchedule.get(wLwScounter);
                        wLwScounter++;
                        } catch (IndexOutOfBoundsException ex) {
                            log.error(wLwScounter + " was out of Bounds.");
                        }
                    }
                    */

                    Color color;
                    switch (info.getStatus()) {
                        case "Disponierbar":
                            color= new Color((float)0.83,(float)0.25,(float)0.23);break;
                        case "Disponiert":
                            color = new Color((float)0.36,(float)0.72,(float)0.36);break;
                        case "Ausfallzeit":
                            prjText = "Ausfallzeit";
                            color = new Color((float)0.47, (float)0.47, (float)0.47);break;
                        case "Angeboten":
                            color = new Color((float)0.36,(float)0.75,(float)0.87);break;
                        case "Geblockt":
                            color = new Color((float)0.94,(float)0.68,(float)0.31);break;
                        default:
                            color = Color.WHITE;break;
                    }
                    Phrase prjPhrase = new Phrase(prjText, baseFontWithSize) ;
                    PdfPCell prjCell = new PdfPCell(new Paragraph(new Phrase(prjPhrase)));
                    prjCell.setColspan(colspan);
                    prjCell.setMinimumHeight(cellHeight);
                    prjCell.setBackgroundColor(color);
//                    prjCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    prjCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    sheduletable.addCell(prjCell);
                }
            }
            sheduletable.completeRow();
        }
        sheduletable.completeRow();
    }

    /**
     * Fills a byte[] with the current schedule (monthly).
     * @param bytes byte[] to fill.
     * @return filled byte[]
     */
    public byte[] fillMonthPdf(byte[] bytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Document document;
            if (!rotate)
                document = new Document(PageSize.A4);
            else document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document,bos);
            document.open();

            PdfPTable sheduleTable = new PdfPTable(yearMonths.size()+1);

            fillMonthTableHead(sheduleTable);
            fillMonthTableBody(sheduleTable);


            Image img = turnToImg(sheduleTable,writer);
            if (yearMonths.size()== 1) {
                try {
                    img.setAbsolutePosition(10,10);
                }catch (NullPointerException ex) {
                    log.error("Error while resizeing Pdf for 1 Month.");
                }
            }
            document.add(img);

            addLegendAndOverAllStats(document, writer);
            addStatistiks(document, writer);

            document.close();

            bytes = bos.toByteArray();
            pdfCreated = true;
        }catch (DocumentException ex) {
            log.error("Can't write to Document while creating pdf.");
        }
        return bytes;
    }

    /**
     * Fills the head of the table.
     * Contains "Mitarbeiter" and the additional (yearMonths.size()) Months.
     * @param sheduleTable PdfPTable to fill.
     */
    private void fillMonthTableHead(PdfPTable sheduleTable) {

        //add Mitarbeiter cell to table
        Phrase p = new Phrase(" Mitarbeiter", baseFontWithSize);
        PdfPCell cell = new PdfPCell(new Paragraph(p));
        cell.setMinimumHeight(cellHeight);
        sheduleTable.addCell(cell);


        //add individual months to table
        for (int month = 0; month < yearMonths.size(); month++) {
            String date = yearMonths.get(month).toString(); //lighter Version
            Phrase monthPhrase = new Phrase(date, baseFontWithSize);
            PdfPCell monthCell = new PdfPCell(new Paragraph(monthPhrase));
            monthCell.setMinimumHeight(cellHeight);
            sheduleTable.addCell(monthCell);
        }
        sheduleTable.completeRow(); //just to be safe
    }

    /**
     * Fills the body of the table.
     * Empty cells, just the color.
     * @param sheduleTable table to color.
     */
    private void fillMonthTableBody(PdfPTable sheduleTable) {

        for (ScheduleRow row : rows) {

            Employee employee = row.getEmployee();
            String name;
            //beautification
            if (!employee.getNameTitle().equals(" "))
                name = " " + employee.getNameTitle() + employee.getFirstName() + " " + employee.getLastName();
            else
                name = employee.getNameTitle() + employee.getFirstName() + " " + employee.getLastName();
            //add name
            Phrase p = new Phrase(name, baseFontWithSize);
            PdfPCell nameCell = new PdfPCell(new Paragraph(p));
            //nameCell.setRowspan(employee.getInformationPacker().size());
            nameCell.setMinimumHeight(cellHeight);
            sheduleTable.addCell(nameCell);


            for (ScheduleElement element : row.getCondensedRow()) {
                Color color;
                switch (element.getStatus().toString()) {
                    case "Disponierbar": color= new Color((float)0.8,(float)0.32,(float)0.32);break;
                    case "Disponiert": color = new Color(0,(float)0.69,(float)0.31);break;
                    case "Ausfallzeit": color = new Color((float)0.47, (float)0.47, (float)0.47);break;
                    case "Angeboten": color = new Color((float)0.20,(float)0.48,(float)0.72);break;
                    case "Geblockt":  color = new Color((float)0.81,(float)0.81,(float)0);break;
                    default: color = Color.WHITE;break;
                }
                Phrase mP = new Phrase("", baseFontWithSize);
                PdfPCell mCell = new PdfPCell(new Paragraph(mP));
                mCell.setMinimumHeight(cellHeight);
                mCell.setBackgroundColor(color);
                sheduleTable.addCell(mCell);
            }
            for (int i = 0; i < employee.getInformationPacker().size(); i++) {
                sheduleTable.completeRow();
            }
        }
        sheduleTable.completeRow();
    }

    /**
     * Turns a PdfPTable into an image.
     * This will allow the table to be resized and perfectly fitted onto a Din A4 page.
     * @param table PdfPTable to resize.
     * @param writer PdfWriter for the Document.
     * @return resized Image (now Din A4).
     */
    private Image turnToImg(PdfPTable table, PdfWriter writer) {
        try {
            //horizontal page
            if (!rotate) {
                table.setTotalWidth(PageSize.A4.getWidth());
                table.setLockedWidth(true);
                PdfContentByte canvas = writer.getDirectContent();
                PdfTemplate template = canvas.createTemplate(table.getTotalWidth(), table.getTotalHeight());
                table.writeSelectedRows(0, -1, 0, table.getTotalHeight(), template);
                Image img = Image.getInstance(template);
                img.scaleToFit(PageSize.A4.getWidth() - 20, PageSize.A4.getHeight() - 20);
                //img.setAlignment(Element.ALIGN_CENTER);
                img.setAbsolutePosition(10, 10);
                //Write to document
                return img;
            }
            //vertical page
            if (rotate) {
                table.setTotalWidth(PageSize.A4.rotate().getWidth());
                table.setLockedWidth(true);
                PdfContentByte canvas = writer.getDirectContent();
                PdfTemplate template = canvas.createTemplate(table.getTotalWidth(), table.getTotalHeight());
                table.writeSelectedRows(0, -1, 0, table.getTotalHeight(), template);
                Image img = Image.getInstance(template);
                img.scaleToFit(PageSize.A4.rotate().getWidth() -20, PageSize.A4.rotate().getHeight()-20);
                //img.setAlignment(Element.ALIGN_CENTER);
                float xOffset = Math.abs(PageSize.A4.rotate().getWidth() - img.getWidth()) +20;
                float yOffset = Math.abs(PageSize.A4.rotate().getHeight() - img.getHeight()) +20;
                xOffset = xOffset/2; yOffset = yOffset/3;
                img.setAbsolutePosition(xOffset, yOffset);
                return img;
            }
        } catch (DocumentException ex) {
            log.error("Can't write Document.");
        }
        return null; //shouldn't happen.
    }

    /**
     * List filled with Workload/Workschedule.
     * Elements will later be added to the table.
     * @return List<String>
     */
    private List<String> getWorkloadAndWorkschedule() {
        List<String> returnMe = new LinkedList<>();
        for (ScheduleRow row : rows) {
            for (ScheduleElement element : row.getCondensedRow()) {
                if (element.getWorkload() != 0 && row.getEmployee().getWorkSchedule() != 0)
                    returnMe.add(element.getWorkload() +"h/"+ row.getEmployee().getWorkSchedule()+"w");
            }
        }
        return returnMe;
    }

    /**
     * Adds the coler legend and overall stats to the pdf on a new page.
     * @param document Document.
     * @param writer PdfWriter.
     */
    private void addLegendAndOverAllStats(Document document, PdfWriter writer) {
        if (document.isOpen()) {
            try {
                document.newPage();

                PdfPTable tab = new PdfPTable(1);
                //color legend:
                PdfPCell leg = new PdfPCell(new Phrase("Legende: \n\n", baseFont));
                leg.setBorder(0);
                tab.addCell(leg);

                PdfPTable innerColorTable = new PdfPTable(1);
                PdfPCell disponierbar = new PdfPCell(new Phrase("Disponierbar", baseFont));
                disponierbar.setBackgroundColor(new Color((float)0.83,(float)0.25,(float)0.23));
                innerColorTable.addCell(disponierbar);
                PdfPCell disponiert = new PdfPCell(new Phrase("Disponiert", baseFont));
                disponiert.setBackgroundColor(new Color((float)0.36,(float)0.72,(float)0.36));
                innerColorTable.addCell(disponiert);
                PdfPCell ausfallzeit = new PdfPCell(new Phrase("Ausfallzeit", baseFont));
                ausfallzeit.setBackgroundColor(new Color((float)0.47, (float)0.47, (float)0.47));
                innerColorTable.addCell(ausfallzeit);
                PdfPCell angeboten = new PdfPCell(new Phrase("Angeboten", baseFont));
                angeboten.setBackgroundColor(new Color((float)0.36,(float)0.75,(float)0.87));
                innerColorTable.addCell(angeboten);
                PdfPCell geblockt = new PdfPCell(new Phrase("Geblockt", baseFont));
                geblockt.setBackgroundColor(new Color((float)0.94,(float)0.68,(float)0.31));
                innerColorTable.addCell(geblockt);

                PdfPCell colorCell = new PdfPCell(innerColorTable);
                tab.addCell(colorCell);

                PdfPCell empty = new PdfPCell(new Phrase("\n\n\n "));
                empty.setBorder(0);
                tab.addCell(empty);

                //overall stats
                PdfPCell statCell = new PdfPCell(new Paragraph(
                        "Allgemein: " + "\n\n" +
                                "Unterbuchte Projekte: " + underbookedPrjs + "\n" +
                                "Davon Pipeline-Projekte: " + pipelinePrjsSize + "\n" +
                                "Laufende Projekte insgesamt: " + projectCount + "\n" +
                                "Unterbuchte Mitarbeiter: " + underbookedEmployees + "\n" +
                                "Mitarbeiter insgesamt: " + employees.size(), baseFont)
                );
                statCell.setBorder(0);
                statCell.setBorderColor(Color.WHITE);
                tab.addCell(statCell);

                document.add(tab);
            } catch (DocumentException ex) {
                log.error("Error while adding Legend and overall stats to pdf.");
            }
        }
    }

    /**
     * Adds the stats to the Document.
     * @param document
     */
    private void addStatistiks(Document document, PdfWriter writer) {
        if(document.isOpen()) {
            //admin
            if (names == null | planned == null | capacity == null | LoginInfo.getCurrentLogin().getPosition() == Employee.Position.ADMINISTRATOR) {
                return;
            }
            //everyone else
            try {
                //new page
                document.newPage();

                //organisationUnit stats
                PdfPTable statTable = new PdfPTable(3);
                //PdfPCell cell0 = new PdfPCell(new Paragraph(new Phrase("Organisationseinheit", FontFactory.getFont(FontFactory.COURIER))));
                PdfPCell cell0 = new PdfPCell(new Paragraph("Statistik Organisationseinheit: \n\n", baseFont));
                cell0.setColspan(3);
                cell0.setBorderColor(Color.WHITE);
                statTable.addCell(cell0);
                statTable.completeRow();

                PdfPCell cell1 = new PdfPCell(new Paragraph(new Phrase(" Mitarbeiter (im Zeitraum)", baseFont)));
                PdfPCell cell2 = new PdfPCell(new Paragraph(new Phrase("Auslastung (in %) \nWerte >100% \n = 100% + Überbuchung." , baseFont)));
                PdfPCell cell3 = new PdfPCell(new Paragraph(new Phrase("Kapazität (in %) \nNegative Werte \n = überbuchte Kapazität ", baseFont)));
                statTable.addCell(cell1);
                statTable.addCell(cell2);
                statTable.addCell(cell3);
                statTable.completeRow();
                if (month) {
                    //How many parts?
                    int partCounter = namesMonth.length/4;
                    int partCounterRest = namesMonth.length - partCounter*4;

                    //Parts which are 4 months each
                    for (int month = 1; month <= partCounter; month++) {
                        PdfPTable fourStatTable = new PdfPTable(statTable);
                        for (int first = (month-1)*4; first < month * 4; first++){
                            PdfPCell timeRow = new PdfPCell(new Phrase(" Zeitraum: " + yearMonths.get(first).toString(),
                                    baseFont));
                            timeRow.setColspan(3);
                            fourStatTable.addCell(timeRow);

                            for (int second = 0; second < namesMonth[first].length; second++) {

                                Phrase p = new Phrase(namesMonth[first][second], baseFont);
                                PdfPCell nameCell = new PdfPCell(new Paragraph(p));
                                fourStatTable.addCell(nameCell);

                                Phrase p1 = new Phrase(Integer.toString(plannedMonth[first][second]), baseFont);
                                PdfPCell c1 = new PdfPCell(new Paragraph(p1));
                                fourStatTable.addCell(c1);

                                Phrase p2 = new Phrase(Integer.toString(capacityMonth[first][second]), baseFont);
                                PdfPCell c2 = new PdfPCell(new Paragraph(p2));
                                fourStatTable.addCell(c2);

                                fourStatTable.completeRow();
                            }

                        }
                        Image img = turnToImg(fourStatTable, writer);
                        document.add(img);
                        document.newPage();
                    }

                    if (partCounterRest > 0) {
                        PdfPTable restTable = new PdfPTable(statTable);
                        for (int rest = 0; rest < partCounterRest; rest++) {

                            int first = partCounter * 4 + rest;

                            PdfPCell timeRow = new PdfPCell(new Phrase(" Zeitraum: " + yearMonths.get(first).toString(),
                                    baseFont));
                            timeRow.setColspan(3);
                            restTable.addCell(timeRow);

                            for (int second = 0; second < namesMonth[first].length; second++) {
                                Phrase p = new Phrase(namesMonth[first][second], baseFont);
                                PdfPCell nameCell = new PdfPCell(new Paragraph(p));
                                restTable.addCell(nameCell);

                                Phrase p1 = new Phrase(Integer.toString(plannedMonth[first][second]), baseFont);
                                PdfPCell c1 = new PdfPCell(new Paragraph(p1));
                                restTable.addCell(c1);

                                Phrase p2 = new Phrase(Integer.toString(capacityMonth[first][second]), baseFont);
                                PdfPCell c2 = new PdfPCell(new Paragraph(p2));
                                restTable.addCell(c2);

                                restTable.completeRow();

                            }
                        }
                        Image img = turnToImg(restTable, writer);
                        document.add(img);
                        document.newPage();
                    }
                }
                else {
                    for (int first = 0; first < names.length; first++) {
                        for (int second = 0; second < names[first].length; second++) {

                            Phrase p = new Phrase(names[first][second], baseFont);
                            PdfPCell nameCell = new PdfPCell(new Paragraph(p));
                            statTable.addCell(nameCell);

                            Phrase plP = new Phrase(Integer.toString(planned[first][second]), baseFont);
                            PdfPCell plC = new PdfPCell(new Paragraph(plP));
                            statTable.addCell(plC);

                            Phrase capP = new Phrase(Integer.toString(capacity[first][second]), baseFont);
                            PdfPCell capC = new PdfPCell(new Paragraph(capP));
                            statTable.addCell(capC);

                            statTable.completeRow();
                        }
                    }
                    Image img = turnToImg(statTable, writer);
                    document.add(img);

                    document.newPage();
                }
            } catch (DocumentException ex) {
                log.error("Error while adding stats to document.");
            }
        }
    }

    /**
     * claculates EmployeeStats
     * @param unitId
     * @param model
     * @param employeeService
     * @param worksOnService
     */
    public void calcEmployeeStats(int unitId, Model model, EmployeeService employeeService, WorksOnService worksOnService) {

        List<Employee> employees = employeeService.getByOrganisationUnit(unitId);

        LocalDate start;
        LocalDate end;

        namesMonth = new String[yearMonths.size()][employees.size()];
        plannedMonth = new int[yearMonths.size()][employees.size()];
        capacityMonth = new int[yearMonths.size()][employees.size()];

        int monthCounter = 0;
        for (YearMonth month : yearMonths) {
            start = month.atDay(1);
            end = month.atEndOfMonth();

            int employeeCounter = 0;
            for (Employee employee : employees) {
                List<WorksOn> worksOns = worksOnService.getByDateAndEmployee(start, end, employee.getId());
                int workLoad = 0;
                for (WorksOn worksOn : worksOns) {
                    workLoad = workLoad + worksOn.getWorkload();
                }
                int workLoadPercent = (workLoad * 100) / employee.getWorkSchedule();

                String name;
                //beautification
                if (!employee.getNameTitle().equals(" "))
                    name = " " + employee.getNameTitle() + employee.getFirstName() + " " + employee.getLastName();
                else
                    name = employee.getNameTitle() + employee.getFirstName() + " " + employee.getLastName();

                namesMonth[monthCounter][employeeCounter] = name;
                plannedMonth[monthCounter][employeeCounter] = workLoadPercent;
                capacityMonth[monthCounter][employeeCounter] = 100 - workLoadPercent;

                employeeCounter++;
            }
            monthCounter++;
        }

        //Use got declined.
        names = new String[1][employees.size()];
        planned = new int[1][employees.size()];
        capacity = new int[1][employees.size()];

        LocalDate today = LocalDate.now();
        start = today.withDayOfMonth(1);
        end =  today.withDayOfMonth(today.lengthOfMonth());

        int counter = 0;
        for (Employee employee : employees) {
            List<WorksOn> worksOns = worksOnService.getByDateAndEmployee(start, end, employee.getId());
            int workload = 0;
            for (WorksOn worksOn : worksOns) {
                workload = workload + worksOn.getWorkload();
            }

            int workloadPrecent = (workload * 100 / employee.getWorkSchedule());

            names[0][counter] = employee.getFirstName() + ' ' + employee.getLastName();
            planned[0][counter] = workloadPrecent;
            capacity[0][counter] = 100 - workloadPrecent;

            counter++;
        }

    }


    /**
     * Maps the Url.
     * Depending on the filename, a filled or empty pdf will be created and, depending on the browser, displayed.
     * @param filename Username
     * @param response created pdf in byte[]
     */
    @RequestMapping(value = "/files/{file_name}/{view}/{month}", method = RequestMethod.GET)
    public void getFile(@PathVariable("file_name") String filename,
                        @PathVariable("view") String view,
                        @PathVariable("month") String month, HttpServletResponse response) {

        try {
            //Make it a Pdf
            response.setContentType("application/pdf");

            pdfCreated = false;
            this.month = false;

            if (filename.equals(LoginInfo.getCurrentLogin().getUsername().toString())) {
                if (view.equals("vertical"))
                    rotate = true;
                if (view.equals("horizontal"))
                    rotate = false;

                //Should work
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (month.equals("normal")) {
                            updateFontSize();
                            source = fillPdf(source);
                        } else if (month.equals("monthly")) {
                            setMonth(true);
                            updateFontSize();
                            source = fillMonthPdf(source);
                        }
                    }
                });
                thread.run();

                pdfCreated = false;
                setMonth(false);

                ByteArrayInputStream bis = new ByteArrayInputStream(source);
                IOUtils.copy(bis, response.getOutputStream());
                response.flushBuffer();

            } else {
                //throws Page not Found
                response.sendError(404);
                pdfCreated = true;
            }

        }catch (IOException ex) {
            log.error("Writing to OutputStream failed.");
        }
    }

    private void setMonth(boolean bool) {
        month = bool;
    }
}
