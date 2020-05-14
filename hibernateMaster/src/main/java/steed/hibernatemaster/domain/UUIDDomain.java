package steed.hibernatemaster.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import steed.ext.util.base.StringUtil;

/**
 * uuid基础实体类,ID注解在方法
 * @author 战马
 *
 */
@MappedSuperclass
public abstract class UUIDDomain extends BaseRelationalDatabaseDomain{
	private static final long serialVersionUID = 8998431532284882361L;
	
	protected String id;

	public UUIDDomain() {
		super();
	}

	public UUIDDomain(String id) {
		super();
		this.id = id;
	}

	@Id
	@GenericGenerator(name="generator",strategy="assigned")
	@GeneratedValue(generator="generator")
	@Column(length=32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

	@Override
	public boolean save() {
		generateId();
		return super.save();
	}

	/**
	  *  生成id,一般调用save方法时才会生成id,但是某些时候需要在save之前就获取实体类id,做其它操作,这时候可以调用该方法提取生成id
	 * @return id
	 * 
	 */
	public String generateId() {
		if (StringUtil.isStringEmpty(id)) {
			id = UUID.randomUUID().toString().replace("-", "");
		}
		return id;
	}
	
}
