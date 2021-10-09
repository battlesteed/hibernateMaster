package steed.hibernatemaster.domain;

import java.util.Date;
import java.util.List;

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
public abstract class SelfIncrementTimeOrderDomain extends BaseRelationalDatabaseDomain{
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
	protected Date addTime;
	protected Date updateTime;
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public boolean update() {
		updateTime = new Date();
		return super.update();
	}
	@Override
	public boolean save() {
		addTime = new Date();
		updateTime = new Date();
		return super.save();
	}
	
	@Override
	public boolean updateNotNullFieldByHql(List<String> updateEvenNull, boolean strictlyMode, String... whereField) {
		updateTime = new Date();
		return super.updateNotNullFieldByHql(updateEvenNull, strictlyMode, whereField);
	}
	
	@Override
	public boolean updateNotNullField(List<String> updateEvenNull) {
		updateTime = new Date();
		return super.updateNotNullField(updateEvenNull);
	}
	
	
}
