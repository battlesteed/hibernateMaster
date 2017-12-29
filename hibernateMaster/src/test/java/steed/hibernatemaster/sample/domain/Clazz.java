package steed.hibernatemaster.sample.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.UUIDDomain;
/**
 * 班级class是关键词,用clazz代替了
 * @author 战马
 *
 */
@Entity
public class Clazz extends UUIDDomain{
	private String name;
	
	private School school;
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ManyToOne
	public School getSchool() {
		return school;
	}
	public void setSchool(School school) {
		this.school = school;
	}
	
}
