package gew.nodemanager.common.model;

import java.util.Objects;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class Pagination {

    private Integer page;

    private Integer size;

    private SortOrder sortOrder;

    private String key;

    private Long total;


    public enum SortOrder {

        ASC,

        DESC
    }

    public Pagination() {
    }

    public Pagination(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public Pagination(Integer page, Integer size, SortOrder sortOrder, String key) {
        this.page = page;
        this.size = size;
        this.sortOrder = sortOrder;
        this.key = key;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagination that = (Pagination) o;
        return Objects.equals(page, that.page) &&
                Objects.equals(size, that.size) &&
                sortOrder == that.sortOrder &&
                Objects.equals(key, that.key) &&
                Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sortOrder, key, total);
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "page=" + page +
                ", size=" + size +
                ", sortOrder=" + sortOrder +
                ", key='" + key + '\'' +
                ", total=" + total +
                '}';
    }
}
