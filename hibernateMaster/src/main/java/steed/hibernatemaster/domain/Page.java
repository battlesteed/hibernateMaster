package steed.hibernatemaster.domain;

import java.io.Serializable;
import java.util.Collection;

public class Page<T> implements Serializable{
	private static final long serialVersionUID = -2774847917304140868L;
	private int currentPage = 1;
	/**
	 * 总页数
	 */
	private int pageInAll;
	/**
	 * 总记录数
	 */
	private Long recordCount;
	/**
	 * 分页大小
	 */
	private int pageSize = 15;
	/**
	 * 实体类集合
	 */
	private Collection<T> domainCollection;
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getPageInAll() {
		return pageInAll;
	}
	public void setPageInAll(int pageInAll) {
		this.pageInAll = pageInAll;
	}
	
	public Long getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(Long recordCount) {
		pageInAll = (int) (recordCount/pageSize);
		if(((long)recordCount % pageSize) != 0){
			pageInAll += 1;
		}
		this.recordCount = recordCount;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public Collection<T> getDomainCollection() {
		return domainCollection;
	}
	public void setDomainCollection(Collection<T> domainCollection) {
		this.domainCollection = domainCollection;
	}
}
