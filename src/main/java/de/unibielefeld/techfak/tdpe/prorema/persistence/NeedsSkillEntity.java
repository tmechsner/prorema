package de.unibielefeld.techfak.tdpe.prorema.persistence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by Brian Baumbach on 12.05.2016.
 */
@Setter
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity(name="NEEDSSKILL")
public class NeedsSkillEntity {

    @Id
    @GeneratedValue
    @Column(name = "NEEDSSKILL_ID")
    private Integer id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "SKILL_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private SkillEntity skill;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "PROJECT_ID")
    @Getter(onMethod = @__({@JsonIdentityReference}))
    private ProjectEntity project;

    @Column(name = "skilllevel")
    private String level;

    public NeedsSkillEntity() {
    }

    public NeedsSkillEntity(ProjectEntity project, SkillEntity skill, String level){
        this.skill = skill;
        this.project = project;
        this.level = level;
    }
}
