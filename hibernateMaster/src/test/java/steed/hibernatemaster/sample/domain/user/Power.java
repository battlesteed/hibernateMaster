package steed.hibernatemaster.sample.domain.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Power extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 8841822962310540390L;
	private String name;
	private String description;
	public Power() {
	}
	public Power(String name) {
		this.name = name;
	}
	@Id
	@GenericGenerator(name="generator",strategy="assigned")
	@GeneratedValue(generator="generator")
	@Column(length=32)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
