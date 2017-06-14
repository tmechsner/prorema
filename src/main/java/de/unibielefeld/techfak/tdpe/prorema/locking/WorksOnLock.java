package de.unibielefeld.techfak.tdpe.prorema.locking;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Created by Martin
 */
@Setter
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
public class WorksOnLock {

    /**
     * Standard duration of a simple lock, set to 30 minutes.
     */
    public static final Duration STANDARD_DURATION = Duration.ofMinutes(3);

    /**
     * Id of the {@link de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity} that holds the lock.
     */
    @NonNull
    private Integer holderId;

    @NonNull
    private Integer orgaId;

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
