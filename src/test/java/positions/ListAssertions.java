package positions;

import java.util.List;

public class ListAssertions {
    public static void assertIsSublist(List<?> mainList, List<?> subList) {
        if (mainList == null || subList == null) {
            throw new IllegalArgumentException("Lists cannot be null");
        }
        if (subList.isEmpty()) {
            return;
        }
        for (Object item : subList) {
            if (!mainList.contains(item)) {
                throw new AssertionError("Item " + item + " not found in the main list");
            }
        }
    }

    public static void assertIsEqual(List<?> list1, List<?> list2) {
        if (list1 == null || list2 == null) {
            throw new IllegalArgumentException("Lists cannot be null");
        }
        if (list1.size() != list2.size()) {
            throw new AssertionError("Lists are not equal in size");
        }
        assertIsSublist(list1, list2);
        assertIsSublist(list2, list1);
    }
}
