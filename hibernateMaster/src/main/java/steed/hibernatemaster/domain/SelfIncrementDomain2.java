package steed.hibernatemaster.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * 主键自增实体类,ID注解在字段
 * @author 战马
 *
 */
@MappedSuperclass
public abstract class SelfIncrementDomain2 extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 8998431532284882361L;
	
	@Id
	@GenericGenerator(name="generator",strategy="native")
	@GeneratedValue(generator="generator")
	@Column(length=32)
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
