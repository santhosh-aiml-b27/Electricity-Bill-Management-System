package util;

import java.util.Collection;

/**
 * Utility for generating unique, readable identifiers for entities.
 */
public class IdGenerator {

    /**
     * Generates a new ID with the given prefix and starting number if none exist.
     * Extracts existing numbers, finds the maximum, and returns prefix + (max + 1).
     *
     * @param prefix e.g. "CUST-", "BILL-", "PAY-"
     * @param existingIds collection of currently existing IDs
     * @param defaultStart starting sequence number, e.g. 1001
     * @return unique formatted ID string
     */
    public static String generateNextId(String prefix, Collection<String> existingIds, int defaultStart) {
        int maxSeq = defaultStart - 1;

        if (existingIds != null) {
            for (String id : existingIds) {
                if (id != null && id.startsWith(prefix)) {
                    String numPart = id.substring(prefix.length());
                    try {
                        int num = Integer.parseInt(numPart);
                        if (num > maxSeq) {
                            maxSeq = num;
                        }
                    } catch (NumberFormatException ignored) {
                        // ignore non-integer suffixes
                    }
                }
            }
        }

        return prefix + (maxSeq + 1);
    }

    public static String nextCustomerId(Collection<String> existingIds) {
        return generateNextId("CUST-", existingIds, 1001);
    }

    public static String nextBillId(Collection<String> existingIds) {
        return generateNextId("BILL-", existingIds, 2001);
    }

    public static String nextPaymentId(Collection<String> existingIds) {
        return generateNextId("PAY-", existingIds, 3001);
    }
}
