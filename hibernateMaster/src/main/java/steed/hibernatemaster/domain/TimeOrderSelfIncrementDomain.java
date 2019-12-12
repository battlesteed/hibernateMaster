package steed.hibernatemaster.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import steed.hibernatemaster.annotation.DefaultOrderBy;

@DefaultOrderBy(value="id",desc = true)
@MappedSuperclass
public class TimeOrderSelfIncrementDomain extends SelfIncrementDomain2{
private static final long serialVersionUID = 6249017384270298464L;
	
	protected Date addTime;
	
	public Date getAddTime() {
		return addTime;
	}
	
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	
	@Override
	public boolean save() {
		addTime = new Date();
		return super.save();
	}
	
}
