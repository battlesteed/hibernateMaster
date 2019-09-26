package steed.hibernatemaster.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * uuid基础实体类,主键不要用自增的
 * (不知道是hibernate的bug还是mysql的bug<span style="text-decoration:line-through;">反正不是在下的bug</span>,
 * 	用自增主键,就算你回滚事务,跟自增主键实体类相关的操作都会生效),
 *  而且自增主键也不利于new sql和分库分表,建议一般用这个UUIDDomain即可
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
