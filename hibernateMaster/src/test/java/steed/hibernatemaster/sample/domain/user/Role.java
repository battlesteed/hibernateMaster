package steed.hibernatemaster.sample.domain.user;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
@Entity
public class Role extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 1L;
	private String name;
	private Set<Power> powerSet;
	private String description;
	/**
	 * 是否属于系统角色(不可删除)
	 */
	private Boolean isSystemRole;
	
	public Boolean getIsSystemRole() {
		if (isSystemRole == null) {
			return false;
		}
		return isSystemRole;
	}
	public void setIsSystemRole(Boolean isSystemRole) {
		this.isSystemRole = isSystemRole;
	}
	public Role(String name) {
		this.name = name;
	}
	public Role() {
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
	
	@ManyToMany(fetch=FetchType.LAZY)
	public Set<Power> getPowerSet() {
		return powerSet;
	}
	public void setPowerSet(Set<Power> powerSet) {
		this.powerSet = powerSet;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public void initialize() {
		Hibernate.initialize(this);
	}
	@Override
	public void initializeAll() {
		initialize();
		for (Power p:getPowerSet()) {
			p.initialize();
		}
	}
}
