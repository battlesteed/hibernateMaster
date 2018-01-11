package steed.hibernatemaster.sample.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import steed.hibernatemaster.domain.UUIDDomain;

@Entity
public class Student extends UUIDDomain{
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
	
	/**
	 * 所属班级
	 */
	private Clazz clazz;
	
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
	@ManyToOne
	public Clazz getClazz() {
		return clazz;
	}
	public void setClazz(Clazz clazz) {
		this.clazz = clazz;
	}
	
}
