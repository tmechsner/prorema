package de.unibielefeld.techfak.tdpe.prorema.locking;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Primary key wrapper to identify a domain.
 *
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
@EqualsAndHashCode
@Setter
@Getter
@AllArgsConstructor
public class DomainIdentifier {

    private Integer domainId;
    private Class domainClass;
}

