package tntfireworks;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvRow {

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();
    private static Logger logger = LoggerFactory.getLogger(CsvRow.class);

    public boolean isValid() {
        Set<ConstraintViolation<CsvRow>> violations = validator.validate(this);
        for (ConstraintViolation<CsvRow> violation : violations) {
            logger.error(violation.getMessage());
        }
        if (violations.size() == 0)
            return true;
        return false;
    }

}
