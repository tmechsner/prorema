package de.unibielefeld.techfak.tdpe.prorema.security.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Matthias on 6/24/16.
 * Is Thrown when access is denied.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class PermissionDeniedException extends RuntimeException {
    @Override
    public String getMessage() {
        return "ACCESS DENIED";
    }
}
