package steed.hibernatemaster.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import steed.hibernatemaster.annotation.DefaultOrderBy;

@DefaultOrderBy(value="addTime",desc = true)
@MappedSuperclass
public class TimeOrderUUIDDomain extends UUIDDomain2{
private static final long serialVersionUID = 6249017384270298464L;
	
	protected Date addTime;

	public TimeOrderUUIDDomain() {
		super();
	}

	public TimeOrderUUIDDomain(String id) {
		super(id);
	}

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
