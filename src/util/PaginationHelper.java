package util;

import java.util.Collections;
import java.util.List;

public class PaginationHelper<T> {

    // Generic pagination method
    public static <T> List<T> paginate(List<T> items, int pageNumber, int pageSize) {
        if (items == null || items.isEmpty())
            return Collections.emptyList();

        int validPageNumber = validatePageNumber(pageNumber, items.size(), pageSize);
        int fromIndex = Math.max((validPageNumber - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, items.size());

        if (fromIndex >= items.size())
            return Collections.emptyList();
        return items.subList(fromIndex, toIndex);
    }

    // Calculate total number of pages
    public static int getTotalPages(int totalItems, int pageSize) {
        if (pageSize <= 0)
            return 0;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    // Validate a page number
    public static int validatePageNumber(int requestedPage, int totalItems, int pageSize) {
        int totalPages = getTotalPages(totalItems, pageSize);
        if (requestedPage < 1)
            return 1;
        if (requestedPage > totalPages)
            return totalPages;
        return requestedPage;
    }
}
