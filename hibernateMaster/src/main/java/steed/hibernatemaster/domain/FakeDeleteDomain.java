package steed.hibernatemaster.domain;

import javax.persistence.MappedSuperclass;

/**
 * 假删除实体类
 * @author 战马
 * @email battle_steed@163.com
 */
@MappedSuperclass
public abstract class FakeDeleteDomain extends BaseDatabaseDomain{
	private static final long serialVersionUID = -539927350380405542L;
	
	private Boolean deleted = false;

	
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public boolean delete() {
		deleted = true;
		return updateNotNullField(null,true);
	}
	
}
