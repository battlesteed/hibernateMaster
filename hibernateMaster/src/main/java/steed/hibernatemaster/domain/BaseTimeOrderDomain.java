package steed.hibernatemaster.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.MappedSuperclass;

import steed.hibernatemaster.annotation.DefaultOrderBy;

@DefaultOrderBy(value="addTime",desc = true)
@MappedSuperclass
public class BaseTimeOrderDomain extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 6249017384270298464L;
	
	protected Date addTime;
	protected Date updateTime;
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public boolean update() {
		updateTime = new Date();
		return super.update();
	}
	@Override
	public boolean save() {
		addTime = new Date();
		updateTime = new Date();
		return super.save();
	}
	
	@Override
	public boolean updateNotNullFieldByHql(List<String> updateEvenNull, boolean strictlyMode, String... whereField) {
		updateTime = new Date();
		return super.updateNotNullFieldByHql(updateEvenNull, strictlyMode, whereField);
	}
	
	@Override
	public boolean updateNotNullField(List<String> updateEvenNull) {
		updateTime = new Date();
		return super.updateNotNullField(updateEvenNull);
	}
	
}
