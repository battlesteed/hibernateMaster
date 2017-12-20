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
	
	private Boolean isDeleted = false;

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public boolean delete() {
		isDeleted = true;
		return updateNotNullField(null,true);
	}
	
}
