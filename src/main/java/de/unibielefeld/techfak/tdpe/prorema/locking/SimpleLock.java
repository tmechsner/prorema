package de.unibielefeld.techfak.tdpe.prorema.locking;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity for simple locking.
 * <p>Used for less frequent modified and less connected domains, like contact, client or skill.</p>
 * <p>These are partly pessimistic (on write actions) and partly optimistic (on read actions) locking.
 * If a lock for a domain exists, it will be able to read from it, but all attempts to write will be prevented, except
 * those from the lock owner.</p>
 * <p>As these locking only apply to a single domain, these should only used on domains, that does not have a great
 * impact
 * on other domains.</p>
 *
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
@Setter
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
public class SimpleLock {

    /**
     * Standard duration of a simple lock, set to 30 minutes.
     */
    public static final Duration STANDARD_DURATION = Duration.ofMinutes(3);

    /**
     * Id of the {@link de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity} that holds the lock.
     */
    @NonNull
    private Integer holderId;

    /**
     * Date and Time of locking.
     */
    @NonNull
    private LocalDateTime beginDateTime;

    /**
     * Duration of this lock.
     * <p>Defaults to STANDARD_DURATION.</p>
     */
    private Duration duration = STANDARD_DURATION;
}
