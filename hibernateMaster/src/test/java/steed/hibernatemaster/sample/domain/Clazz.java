package steed.hibernatemaster.sample.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
/**
 * 班级class是关键词,用clazz代替了
 * @author 战马
 *
 */
@Entity
public class Clazz extends BaseRelationalDatabaseDomain{
	/**
	 * 
	 */
	private Long id;
	private String name;
	@Id
	@GenericGenerator(name="gen1",strategy="assigned")
	@GeneratedValue(generator="gen1")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
