package org.trimou.basis;

/**
 *
 * @author Martin Kouba
 */
public interface HealthCheck {

    /**
     *
     * @return a unique identifier
     */
    String getId();

    /**
     * Performs the check.
     *
     * @return the result
     */
    Result perform();

    /**
     *
     *
     */
    static final class Result {

        static final Result SUCCESS = new Result(true, null);

        static Result failure(String details) {
            return new Result(false, details);
        }

        private final boolean isOk;

        private final String details;

        private Result(boolean isOk, String details) {
            this.isOk = isOk;
            this.details = details;
        }

        boolean isOk() {
            return isOk;
        }

        String getDetails() {
            return details;
        }

        boolean hasDetails() {
            return details != null;
        }

    }

}
