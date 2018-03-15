package steed.hibernatemaster.sample.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import steed.hibernatemaster.domain.UUIDDomain;
/**
 * 班级class是关键词,用clazz代替了
 * @author 战马
 *
 */
@Entity
public class Clazz extends UUIDDomain{
	
	@NotBlank(message="班级名字必填")
	private String name;
	
	private School school;
	
	/**
	 * 学生数量
	 */
	@NotNull(message="学生数量不能为null")
	@Min(value=5,message="学生数量不能小于5")
	@Max(100)
	private Integer studentCount;
	
	
	public Integer getStudentCount() {
		return studentCount;
	}
	public void setStudentCount(Integer studentCount) {
		this.studentCount = studentCount;
	}
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
