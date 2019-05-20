package steed.hibernatemaster.domain;

import java.io.Serializable;

import javax.persistence.Transient;

import steed.ext.util.base.DomainUtil;



public abstract class BaseDomain implements Serializable{
	private static final long serialVersionUID = 3930474378471185938L;
	private Object tempData;
	
	public void setVirtualID(Serializable serializable){
		DomainUtil.setDomainId(this,serializable);
	}
	@Transient
	public Serializable getVirtualID(){
		return DomainUtil.getDomainId(this);
	}
	
	@Transient
	public Object getTempData() {
		return tempData;
	}

	public void setTempData(Object tempData) {
		this.tempData = tempData;
	}

	@Override
	public int hashCode() {
		return DomainUtil.domainHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		return DomainUtil.domainEquals(this, obj);
	}
}
