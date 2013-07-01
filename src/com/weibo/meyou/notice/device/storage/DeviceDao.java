package com.weibo.meyou.notice.device.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.weibo.meyou.notice.model.Device;
import com.weibo.wejoy.data.dao.impl.ClusterDatabases;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-12
 */
public class DeviceDao implements IDeviceDao {
	private static final Logger log = Logger.getLogger(DeviceDao.class);
	
	private static final String DEVICE_QUERY_SQL="select * from closefriend_device where userid=?";
	
	private static final String DEVICE_SAVE_SQL = 
			"replace into closefriend_device(userid,device_id,data_type,start_time,end_time,display) " +
			"values(?,?,?,?,?,?)";
	
	private static final String DEVICE_UPDATE_SQL = "update closefriend_device set device_id=?, data_type=?, start_time=?, " +
			"end_time=?, display=? where userid=?";
	
	private static final String DEVICE_DELETE_SQL = "delete from closefriend_device where userid=?";
	
	private ClusterDatabases clusterDatabases;
	public String strategykey;
	
	public ClusterDatabases getClusterDatabases() {
		return clusterDatabases;
	}

	public void setClusterDatabases(ClusterDatabases clusterDatabases) {
		this.clusterDatabases = clusterDatabases;
	}
	
	
	@SuppressWarnings("unchecked")
	public Device getDeviceByUserId(long userid) {
		Device device=null;
		List<Device> list = this.clusterDatabases.getIdxJdbcTemplate(String.valueOf(userid), strategykey)
				.query(DEVICE_QUERY_SQL, new Object[]{userid}, new DeviceRowMapper());
		if(list!=null && !list.isEmpty()){
			device=list.get(0);
		}
		return device;
	}

	private class DeviceRowMapper implements RowMapper{

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Device device=new Device();
			device.setUserid(rs.getLong("userid"));
			device.setDeviceId(rs.getString("device_id"));
			device.setSwitchInfo(rs.getInt("data_type"));
			device.setStartTime(rs.getInt("start_time"));
			device.setEndTime(rs.getInt("end_time"));
			device.setDisplay(rs.getInt("display"));
			return device;
		}
		
	}
	
	public boolean saveDevice(Device device) {
		int result = 0;
		
		try {
			result = this.clusterDatabases.getIdxJdbcTemplate(String.valueOf(device.getUserid()), strategykey)
					.update(DEVICE_SAVE_SQL, new Object[]{
							device.getUserid(),device.getDeviceId(),device.getSwitchInfo(),
							device.getStartTime(),device.getEndTime(),device.getDisplay()
					});
		} catch (Exception e) {
			log.error("DeviceDao.saveDevice exception", e);
		}
		
		
		return result > 0;
	}
	
	public boolean updateDevice(Device device){
		int result = this.clusterDatabases.getIdxJdbcTemplate(String.valueOf(device.getUserid()), strategykey)
				.update(DEVICE_UPDATE_SQL, new Object[]{
						device.getDeviceId(),device.getSwitchInfo(),
						device.getStartTime(),device.getEndTime(),device.getDisplay(),
						device.getUserid()
				});
			
			return result > 0;
	}
	
	public boolean delDevice(Device device){
		int result = this.clusterDatabases.getIdxJdbcTemplate(String.valueOf(device.getUserid()), strategykey)
				.update(DEVICE_DELETE_SQL, new Object[]{ device.getUserid() });
			
			return result > 0;
	}	
	
	@Override
	public String toString() {
		return new StringBuilder().
				append("strategykey:").append(strategykey).
				toString();
	}


}
