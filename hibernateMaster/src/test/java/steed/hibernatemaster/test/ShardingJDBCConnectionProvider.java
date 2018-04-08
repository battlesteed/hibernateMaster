package steed.hibernatemaster.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.jdbc.Driver;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;

public class ShardingJDBCConnectionProvider implements ConnectionProvider {
	private DataSource dataSource;
	public ShardingJDBCConnectionProvider() {
		dataSource = buildDataSource();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		try {
			return dataSource.isWrapperFor(unwrapType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		try {
			return dataSource.unwrap(unwrapType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}
	

    private DataSource buildDataSource() {
        //设置分库映射
    	Map<String, DataSource> dataSourceMap = new HashMap<>();
    	dataSourceMap.put("masterDataSource", createDataSource("hibernateMaster"));
    	dataSourceMap.put("slaveDataSource0", createDataSource("hibernateMaster1"));
    	
    	MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("demo_ds_master_slave");
        masterSlaveRuleConfig.setMasterDataSourceName("masterDataSource");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("slaveDataSource0"));

    	DataSource dataSource = null;
		try {
			dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, new HashMap<>());
			dataSource.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

        return dataSource;
    }
    
    private static DataSource createDataSource(final String dataSourceName) {
        //使用druid连接数据库
        DruidDataSource result = new DruidDataSource();
        result.setDriverClassName(Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3307/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}


}
