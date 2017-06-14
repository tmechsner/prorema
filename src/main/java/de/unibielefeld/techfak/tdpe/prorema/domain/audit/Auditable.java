package de.unibielefeld.techfak.tdpe.prorema.domain.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass that adds the capability of tracking changes.
 * Created by timo on 20.05.16.
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class Auditable {

    /**
     * List of changelog entries.
     */
    protected List<ChangelogEntry> historyEntries = new ArrayList<>();

    /**
     * Add a new changelog entry in case that... <br/>
     * ...the object has been created,<br/>
     * ...the object has been updated or<br/>
     * ...the object has been deleted.<br/>
     * @param entry
     */
    public void addHistoryEntry(ChangelogEntry entry) {
        historyEntries.add(entry);
    }

    /**
     *
     * @return
     */
    public List<ChangelogEntry> getHistory() { return historyEntries; };

}
