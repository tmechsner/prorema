package de.unibielefeld.techfak.tdpe.prorema.utils;

import java.util.List;

/**
 * Created by mrott on 10.05.16.
 *
 * A list knowing the size it would have, if no security was applied.
 */
public class ServiceList<T> {
    int allEntities;
    int allowedLength;
    List<T> list;

    public ServiceList(int allowedLength, int allEntities, List<T> list) {
        this.allowedLength = allowedLength;
        this.allEntities = allEntities;
        this.list = list;
    }

    public int getAllowedLength() {
        return allowedLength;
    }

    public int getAllEntities() {
        return allEntities;
    }

    public List<T> getList() {
        return list;
    }
}
