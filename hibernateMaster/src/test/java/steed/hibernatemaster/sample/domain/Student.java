package steed.hibernatemaster.sample.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;

@Entity
public class Student extends BaseRelationalDatabaseDomain{
	/**
	 * 学号
	 */
	private String studentNumber;
	private Integer sex;
	private String name;
	/**
	 * 入学日期
	 */
	private Date inDate;
	@Id
	public String getStudentNumber() {
		return studentNumber;
	}
	public void setStudentNumber(String studentNumber) {
		this.studentNumber = studentNumber;
	}
	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getInDate() {
		return inDate;
	}
	public void setInDate(Date inDate) {
		this.inDate = inDate;
	}
	
}
