package steed.hibernatemaster.sample.domain;

import java.util.Date;

import javax.persistence.Entity;

import steed.hibernatemaster.domain.UUIDDomain;

@Entity
public class School extends UUIDDomain{
	/**
	 * 学校名称
	 */
	private String name;
	/**
	 * 校长,一把手
	 */
	private String chargeMan;
	
	/**
	 * 建校日期
	 */
	private Date buildDate;
	/**
	 * 校训
	 */
	private String motto;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getChargeMan() {
		return chargeMan;
	}
	public void setChargeMan(String chargeMan) {
		this.chargeMan = chargeMan;
	}
	
	public Date getBuildDate() {
		return buildDate;
	}
	public void setBuildDate(Date buildDate) {
		this.buildDate = buildDate;
	}
	public String getMotto() {
		return motto;
	}
	public void setMotto(String motto) {
		this.motto = motto;
	}
}
