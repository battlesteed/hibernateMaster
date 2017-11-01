package steed.hibernatemaster.domain;

import java.io.Serializable;


/**
 * 联合主键类必须实现的接口
 * @author 战马
 */
public interface UnionKeyDomain{
	/**
	 * 获取实体类联合主键
	 * @return 实体类联合主键
	 */
	public DomainID getDomainID();
	public void setDomainID(Serializable unionKeyDomain);
}
