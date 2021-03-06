package steed.hibernatemaster.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * 主键自增实体类,ID注解在方法
 * @author 战马
 *
 */
@MappedSuperclass
public abstract class SelfIncrementDomain extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 8998431532284882361L;
	
	protected Long id;

	@Id
	@GenericGenerator(name="generator",strategy="native")
	@GeneratedValue(generator="generator")
	@Column(length=32)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
