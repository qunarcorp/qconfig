package qunar.tc.qconfig.common.bean;

import java.util.List;

/**
 * 存储分页结果
 *
 * Created by chenjk on 2017/5/12.
 */
public class PaginationResult<T> {
    private List<T> data;//数据

    private int totalPage;//总共页数

    private long total;//总计条数

    private int pageSize;//每页条数

    private int page;//当前第几页

    public PaginationResult() {
    }

    public PaginationResult(List<T> data, int page, int pageSize, long total) {
        this.data = data;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPage = (int)((total - 1) / pageSize + 1);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
