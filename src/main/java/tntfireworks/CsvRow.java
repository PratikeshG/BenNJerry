package tntfireworks;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Base class for rows from a CSV file, housing the javax Validation settings
 */
public class CsvRow {

    /*
     * Checks and logs the errors found in javax Validator (e.g. @NotNull)
     * 
     * @return true if no validation errors, false otherwise
     */
    public boolean isValid() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Logger logger = LoggerFactory.getLogger(CsvRow.class);

        Set<ConstraintViolation<CsvRow>> violations = validator.validate(this);
        for (ConstraintViolation<CsvRow> violation : violations) {
            logger.error(violation.getMessage());
        }
        if (violations.size() == 0) {
            return true;
        }
        return false;
    }

}
