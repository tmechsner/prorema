package de.unibielefeld.techfak.tdpe.prorema.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewArchiveCmd;
import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewClientCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.*;
import de.unibielefeld.techfak.tdpe.prorema.domain.audit.ChangelogEntry;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.*;
import de.unibielefeld.techfak.tdpe.prorema.locking.DomainIdentifier;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.*;
import de.unibielefeld.techfak.tdpe.prorema.persistence.audit.ChangelogEntryEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.*;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import lombok.extern.log4j.Log4j2;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import javax.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool.client;
import static de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool.employee;

/**
 * Controller for the Archiving
 *
 * Contains the Get and Set Methods of the webpages and the Logik of the Archiving
 */

@Controller
@Log4j2
public class ArchiveController extends ErrorHandler {

    //Magicnumbers for the time intervals
    private static final String ARCHIVEFORM = "archiveform";
    private static final String TWOYEARS = "2";
    private static final String THREEYEARS = "3";
    private static final String FOURYEARS = "4";
    private static final String FIVEYEARS = "5";

    // Services of the Database
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final EmployeeProfileService employeeProfileService;
    private final HasSkillService hasSkillService;
    private final NeedsSkillService needsSkillService;
    private final WorksOnService worksOnService;

    private final ChangelogEntryRepository changelogEntryRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final WorksOnRepository worksOnRepository;
    private final HasSkillRepository hasSkillRepository;
    private final NeedsSkillRepository needsSkillRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    @Value("${export.path}")
    private String exportPath;

    //Mapper for JSON
    private final ObjectMapper archiveMapper = new ObjectMapper();


    /**
     * Constructor of the ArchiveController
     * sets all the final variables
     *
     * @param projectService
     * @param employeeService
     * @param employeeRepository
     * @param changelogEntryRepository
     * @param employeeProfileService
     * @param hasSkillService
     * @param hasSkillRepository
     * @param needsSkillService
     * @param worksOnService
     * @param projectRepository
     * @param worksOnRepository
     * @param needsSkillRepository
     * @param employeeProfileRepository
     */
    @Autowired
    public ArchiveController(ProjectService projectService, EmployeeService employeeService, EmployeeRepository employeeRepository, ChangelogEntryRepository changelogEntryRepository,
                             EmployeeProfileService employeeProfileService, HasSkillService hasSkillService, HasSkillRepository hasSkillRepository, NeedsSkillService needsSkillService,
                             WorksOnService worksOnService, ProjectRepository projectRepository, WorksOnRepository worksOnRepository, NeedsSkillRepository needsSkillRepository,
                             EmployeeProfileRepository employeeProfileRepository){

        this.projectService = projectService;
        this.employeeService = employeeService;
        this.changelogEntryRepository = changelogEntryRepository;
        this.employeeProfileService = employeeProfileService;
        this.hasSkillService = hasSkillService;
        this.needsSkillService = needsSkillService;
        this.worksOnService = worksOnService;

        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.worksOnRepository = worksOnRepository;
        this.hasSkillRepository = hasSkillRepository;
        this.needsSkillRepository = needsSkillRepository;
        this.employeeProfileRepository = employeeProfileRepository;

    }


    /**
     * Mapping of archive form.
     *
     * @param model Modell
     * @return archiveform
     */
    @RequestMapping(value = "/archiveform", method = RequestMethod.GET)
    public String showArchiveForm(NewArchiveCmd newArchiveCmd, Model model) {
        try {

            List<String> years = new ArrayList<>();
            List<String> archiveFiles = new ArrayList<>();
            File archiveFolder = new File(exportPath);
            try {
                for (File c : archiveFolder.listFiles()) {
                    String name = c.getName();
                    if(name.endsWith(".json") && name.length() >= 16) {
                        years.add(name.substring(12, 16));
                        archiveFiles.add(name);
                    }
                }
            }catch (NullPointerException e){
                years.add("keine Archiv-Dateien vorhanden");
            }
            Collections.sort(archiveFiles);
            Collections.sort(years);
            model.addAttribute("years", years);
            model.addAttribute("archiveFiles", archiveFiles);
            model.addAttribute("position", LoginInfo.getPosition().toString());

        } catch (Exception e) {
            log.error("Error showing archive form.", e);
            throw e;
        }
        return ARCHIVEFORM;
    }

    /**
     * Gets the information from the archiveform
     *
     * @param newArchiveCmd NewArchiveCmd
     * @param bindingResult BindingResult
     * @param model Model
     * @return archiveform
     * @throws PermissionDeniedException
     */
    @RequestMapping(value = "/archiveform", method = RequestMethod.POST)
    public String saveArchive(@Valid NewArchiveCmd newArchiveCmd, BindingResult bindingResult, Model model) throws
            PermissionDeniedException {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("newArchiveCmd", newArchiveCmd);
                model.addAttribute("position", LoginInfo.getPosition().toString());
                log.debug("Archive form input not valid.");
                return ARCHIVEFORM;
            } else {
                switch (newArchiveCmd.getAction()) {
                    case "write":
                        writeArchive(newArchiveCmd.getDate());
                        break;
                    case "read":
                        readArchive(Integer.parseInt(newArchiveCmd.getDate()));
                        break;
                    default:
                        break;
                }

                try {

                    File archiveFolder = new File(exportPath);

                    List<String> years = new ArrayList<>();
                    List<String> archiveFiles = new ArrayList<>();
                    try {
                        for (File c : archiveFolder.listFiles()) {
                            String name = c.getName();
                            if(name.endsWith(".json") && name.length() >= 16) {
                                archiveFiles.add(c.getName());
                                years.add(c.getName().substring(12, 16));
                            }
                        }
                    }catch (NullPointerException e){
                        years.add("keine Archiv-Dateien vorhanden");
                    }
                    Collections.sort(archiveFiles);
                    Collections.sort(years);
                    model.addAttribute("years", years);
                    model.addAttribute("archiveFiles", archiveFiles);
                    model.addAttribute("position", LoginInfo.getPosition().toString());

                } catch (Exception e) {
                    log.error("Error showing archive form.", e);
                    throw e;
                }
                return ARCHIVEFORM;
            }
        } catch (Exception e) {
            log.error("Error evaluating archive form.", e);
            throw e;
        }
    }


    /**
     * Writes all Entities of Employee and Project and their relations in a JSON File
     * which were odler then x -years
     *
     * @param date String
     */
    private void writeArchive(String date){

        List<ProjectEntity> projectEntities = projectService.getAllEntities();
        List<EmployeeEntity> employeeEntities = employeeService.getAllEntities();
        List<EmployeeProfileEntity> employeeProfileEntities = employeeProfileService.getAllEntities();
        List<HasSkillEntity> hasSkillEntities = hasSkillService.getAllEntities();
        List<NeedsSkillEntity> needsSkillEntities = needsSkillService.getAllEntities();
        List<WorksOnEntity> worksOnEntities = worksOnService.getAllEntities();
        List<ChangelogEntryEntity> changelogEntryEntities = new ArrayList<>();
        List<ProjectEntity> tmpPro = new ArrayList<>();
        List<EmployeeEntity> tmpEmp = new ArrayList<>();
        List<NeedsSkillEntity> tmpNeed = new ArrayList<>();
        List<WorksOnEntity> tmpWorks = new ArrayList<>();
        List<HasSkillEntity> tmpHas = new ArrayList<>();
        List<EmployeeProfileEntity> tmpProf = new ArrayList<>();
        List<ChangelogEntryEntity> tmpChLog = new ArrayList<>();

        Iterable<ChangelogEntryEntity> saveChLog = changelogEntryRepository.findAll();
        for (ChangelogEntryEntity entity : saveChLog) {
            changelogEntryEntities.add(entity);
        }


        LocalDate archiveDate = LocalDate.of(LocalDate.now().getYear(), 1 , 1);
        LocalDate tmpDate = LocalDate.now();


        switch (date) {
            case FIVEYEARS:
                archiveDate = archiveDate.minusYears(5);
                break;
            case FOURYEARS:
                archiveDate = archiveDate.minusYears(4);
                break;
            case THREEYEARS:
                archiveDate = archiveDate.minusYears(3);
                break;
            case TWOYEARS:
                archiveDate = archiveDate.minusYears(2);
                break;
            default:
                archiveDate = archiveDate.minusYears(7);
                break;
        }



        //Select all Entities for the data backup

        for (Iterator<ProjectEntity> iter = projectEntities.iterator(); iter.hasNext(); ) {
            ProjectEntity pro = iter.next();
            if (pro.getEndDate().isAfter(archiveDate) || pro.getEndDate() == null) {
                iter.remove();
            }
        }

        for (Iterator<EmployeeEntity> iter = employeeEntities.iterator(); iter.hasNext(); ) {
            EmployeeEntity emp = iter.next();
            if (emp.getWorkExit().isAfter(archiveDate) || emp.getWorkExit() == null) {
                iter.remove();
            }
        }


        //Select all other related Entitites


        for (ProjectEntity pro:projectEntities) {
            // Needskills
            for (NeedsSkillEntity need:needsSkillEntities) {
                if (need.getProject().getId().equals(pro.getId())) {
                    tmpNeed.add(need);
                }
            }
            // WorksOn
            for (Iterator<WorksOnEntity> iter = worksOnEntities.iterator(); iter.hasNext(); ) {
                WorksOnEntity works = iter.next();
                if (works.getProject().getId().equals(pro.getId())) {
                    tmpWorks.add(works);
                    iter.remove();
                }
            }
            // ChangeLogs
            for (Iterator<ChangelogEntryEntity> iter = changelogEntryEntities.iterator(); iter.hasNext(); ) {
                ChangelogEntryEntity chLog = iter.next();
                if (chLog.getAuditableEntity().getId().equals(pro.getId())) {
                    tmpChLog.add(chLog);
                    iter.remove();
                }
            }
        }


        for (EmployeeEntity emp:employeeEntities) {
            // HasSkill
            for (HasSkillEntity has:hasSkillEntities) {
                if (has.getEmployee().getId().equals(emp.getId())) {
                    tmpHas.add(has);
                }
            }
            // EmployeeProfile
            for (EmployeeProfileEntity empProfile:employeeProfileEntities) {
                if (empProfile.getEmployee().getId().equals(emp.getId())) {
                    tmpProf.add(empProfile);
                }
            }
            // WorksOn
            for (Iterator<WorksOnEntity> iter = worksOnEntities.iterator(); iter.hasNext(); ) {
                WorksOnEntity works = iter.next();
                if (works.getEmployee().getId().equals(emp.getId())) {
                    tmpWorks.add(works);
                    iter.remove();
                }
            }
            // ChangeLogs
            for (Iterator<ChangelogEntryEntity> iter = changelogEntryEntities.iterator(); iter.hasNext(); ) {
                ChangelogEntryEntity chLog = iter.next();
                if (chLog.getAuditableEntity().getId().equals(emp.getId())) {
                    tmpChLog.add(chLog);
                    iter.remove();
                }
            }
        }

        tmpPro = new ArrayList<>(projectEntities);
        tmpEmp = new ArrayList<>(employeeEntities);


        // Delet the Entities from the Database

        for (NeedsSkillEntity need:tmpNeed) {
            needsSkillService.delete(need.getId());
        }
        for (HasSkillEntity has:tmpHas) {
            hasSkillService.delete(has.getId());
        }
        for (EmployeeProfileEntity empProfile:tmpProf) {
            employeeProfileService.delete(empProfile.getId());
        }
        for (WorksOnEntity works:tmpWorks) {
            worksOnService.delete(works.getId());
        }
        for (ChangelogEntryEntity chLog:tmpChLog) {
            changelogEntryRepository.delete(chLog);
        }
        for (ProjectEntity pro:tmpPro) {
            projectService.delete(pro.getId());
        }
        for (EmployeeEntity emp:tmpEmp) {
            employeeService.delete(emp.getId());
        }


        // Save the list in a file


        for (ProjectEntity pro:tmpPro) {
            if(pro.getEndDate().isBefore(tmpDate)){
                tmpDate = pro.getEndDate();
            }
        }
        for (EmployeeEntity emp:tmpEmp) {
            if(emp.getWorkExit().isBefore(tmpDate)){
                tmpDate = emp.getWorkExit();
            }
        }

        tmpDate = LocalDate.of(tmpDate.getYear(), 1 , 1);

        // No WorksOn in the Archive, does not work yet
        tmpWorks.clear();

        while(tmpDate.isBefore(archiveDate)){
            tmpDate = tmpDate.plusYears(1);

            projectEntities.clear();
            employeeEntities.clear();
            hasSkillEntities.clear();
            needsSkillEntities.clear();
            employeeProfileEntities.clear();
            worksOnEntities.clear();


            for (Iterator<ProjectEntity> iter = tmpPro.iterator(); iter.hasNext(); ) {
                ProjectEntity pro = iter.next();
                if (pro.getEndDate().isBefore(tmpDate)) {
                    projectEntities.add(pro);
                    iter.remove();
                }
            }

            for (Iterator<EmployeeEntity> iter = tmpEmp.iterator(); iter.hasNext(); ) {
                EmployeeEntity emp = iter.next();
                if (emp.getWorkExit().isBefore(tmpDate)) {
                    employeeEntities.add(emp);
                    iter.remove();
                }
            }


            for (ProjectEntity pro:projectEntities) {
                // Needskills
                for (Iterator<NeedsSkillEntity> iter = tmpNeed.iterator(); iter.hasNext(); ) {
                    NeedsSkillEntity need = iter.next();
                    if (need.getProject().getId().equals(pro.getId())) {
                        needsSkillEntities.add(need);
                        iter.remove();
                    }
                }
                // WorksOn
                for (Iterator<WorksOnEntity> iter = tmpWorks.iterator(); iter.hasNext(); ) {
                    WorksOnEntity works = iter.next();
                    if (works.getProject().getId().equals(pro.getId())) {
                        worksOnEntities.add(works);
                        iter.remove();
                    }
                }
            }


            for (EmployeeEntity emp:employeeEntities) {
                // HasSkill
                for (Iterator<HasSkillEntity> iter = tmpHas.iterator(); iter.hasNext(); ) {
                    HasSkillEntity has = iter.next();
                    if (has.getEmployee().getId().equals(emp.getId())) {
                        hasSkillEntities.add(has);
                        iter.remove();
                    }
                }
                // EmployeeProfile
                for (Iterator<EmployeeProfileEntity> iter = tmpProf.iterator(); iter.hasNext(); ) {
                    EmployeeProfileEntity empProfile = iter.next();
                    if (empProfile.getEmployee().getId().equals(emp.getId())) {
                        employeeProfileEntities.add(empProfile);
                        iter.remove();
                    }
                }
                // WorksOn
                for (Iterator<WorksOnEntity> iter = tmpWorks.iterator(); iter.hasNext(); ) {
                    WorksOnEntity works = iter.next();
                    if (works.getEmployee().getId().equals(emp.getId())) {
                        worksOnEntities.add(works);
                        iter.remove();
                    }
                }
            }

            // put the lists into a file with the name "backup_from_" +tmpDate.getYear()-1

            try{

                String jsonProjectEntities = archiveMapper.writeValueAsString(projectEntities);
                String jsonEmployeeEntities = archiveMapper.writeValueAsString(employeeEntities);
                String jsonWorksOnEntities = archiveMapper.writeValueAsString(worksOnEntities);
                String jsonHasSkillEntities = archiveMapper.writeValueAsString(hasSkillEntities);
                String jsonNeedsSkillEntities = archiveMapper.writeValueAsString(needsSkillEntities);
                String jsonEmployeeProfileEntities = archiveMapper.writeValueAsString(employeeProfileEntities);

                String[] archiveLists = {jsonProjectEntities, jsonEmployeeEntities, jsonWorksOnEntities, jsonHasSkillEntities, jsonNeedsSkillEntities, jsonEmployeeProfileEntities};
                String path = exportPath + "/backup_from_" + (tmpDate.getYear()-1) + ".json";


                archiveMapper.writeValue( new File(path) , archiveLists);

            }catch (Exception e){
                log.error(e);
            }
        }

    }

    /**
     * Read all files from date to latest and save the Entities in the database
     * Its start with the most recent File and counts down to the oldest (=date)
     *
     * @param date int
     */

    private void readArchive(int date) {

        HashMap<Integer,ProjectEntity> projectEntityHashMap = new HashMap<>();
        HashMap<Integer,EmployeeEntity> employeeEntityHashMap = new HashMap<>();

        int lowestDate = date;

        File archiveFolder = new File(exportPath);

        try {
            for (File c : archiveFolder.listFiles()) {
                String name = c.getName();
                if(name.endsWith(".json") && name.length() >= 16) {
                    int tmpDate = Integer.parseInt(c.getName().substring(12, 16));
                    if (tmpDate > date) {
                        date = tmpDate;
                    }
                }
            }
        }catch (NullPointerException e){
            date = 0;
        }



        for(;date >= lowestDate; date--) {

            List<ProjectEntity> projectEntities = new ArrayList<>();
            List<EmployeeEntity> employeeEntities = new ArrayList<>();
            List<WorksOnEntity> worksOnEntities = new ArrayList<>();
            List<HasSkillEntity> hasSkillEntities = new ArrayList<>();
            List<NeedsSkillEntity> needsSkillEntities = new ArrayList<>();
            List<EmployeeProfileEntity> employeeProfileEntities = new ArrayList<>();

            // Try to read the jsonString of the lists from the File
            try {

                String path = exportPath + "/backup_from_" + date + ".json";

                String[] archiveLists = archiveMapper.readValue(new File(path), String[].class);

                String jsonProjectEntities = archiveLists[0];
                String jsonEmployeeEntities = archiveLists[1];
                String jsonWorksOnEntities = archiveLists[2];
                String jsonHasSkillEntities = archiveLists[3];
                String jsonNeedsSkillEntities = archiveLists[4];
                String jsonEmployeeProfileEntities = archiveLists[5];

                projectEntities = archiveMapper.readValue(jsonProjectEntities, new TypeReference<List<ProjectEntity>>() { });
                employeeEntities = archiveMapper.readValue(jsonEmployeeEntities, new TypeReference<List<EmployeeEntity>>() { });
                worksOnEntities = archiveMapper.readValue(jsonWorksOnEntities, new TypeReference<List<WorksOnEntity>>() { });
                hasSkillEntities = archiveMapper.readValue(jsonHasSkillEntities, new TypeReference<List<HasSkillEntity>>() { });
                needsSkillEntities = archiveMapper.readValue(jsonNeedsSkillEntities, new TypeReference<List<NeedsSkillEntity>>() { });
                employeeProfileEntities = archiveMapper.readValue(jsonEmployeeProfileEntities, new TypeReference<List<EmployeeProfileEntity>>() { });

            } catch (Exception e) {
                e.printStackTrace();
            }

            for (EmployeeEntity emp:employeeEntities) {
                Integer oldId = emp.getId();
                emp.setId(null);
                EmployeeEntity newEmp = employeeRepository.save(emp);
                employeeEntityHashMap.put(oldId, newEmp);
            }

            for (ProjectEntity pro:projectEntities) {
                Integer oldId = pro.getId();
                pro.setId(null);
                if(employeeEntityHashMap.containsKey(pro.getProjectManager().getId())){
                    pro.setProjectManager(employeeEntityHashMap.get(pro.getProjectManager().getId()));
                    System.out.println("has changed" + pro.getProjectManager().getId());
                }
                ProjectEntity newPro = projectRepository.save(pro);
                projectEntityHashMap.put(oldId, newPro);
            }

            for (WorksOnEntity work:worksOnEntities) {
                work.setId(null);
                if(employeeEntityHashMap.containsKey(work.getEmployee().getId())) {
                    work.setEmployee(employeeEntityHashMap.get(work.getEmployee().getId()));
                }
                if(projectEntityHashMap.containsKey(work.getProject().getId())) {
                    work.setProject(projectEntityHashMap.get(work.getProject().getId()));
                }
                worksOnRepository.save(work);
            }

            for (HasSkillEntity has:hasSkillEntities) {
                has.setId(null);
                has.setEmployee(employeeEntityHashMap.get(has.getEmployee().getId()));
                hasSkillRepository.save(has);
            }

            for (NeedsSkillEntity need:needsSkillEntities) {
                need.setId(null);
                need.setProject(projectEntityHashMap.get(need.getProject().getId()));
                needsSkillRepository.save(need);
            }

            for (EmployeeProfileEntity empProfile:employeeProfileEntities) {
                empProfile.setId(null);
                empProfile.setEmployee(employeeEntityHashMap.get(empProfile.getEmployee().getId()));
                employeeProfileRepository.save(empProfile);
            }

        }

    }

}
