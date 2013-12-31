package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataSetProvider {
	
	private DataSetMD dataSetMD;
	private CuratorMD curatorMD;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	private Resource tdbResource;
	public DataSetMD getDataSetMD() {
		return dataSetMD;
	}
	public void setDataSetMD(DataSetMD dataSetMD) {
		this.dataSetMD = dataSetMD;
	}
	public CuratorMD getCuratorMD() {
		return curatorMD;
	}
	public void setCuratorMD(CuratorMD curatorMD) {
		this.curatorMD = curatorMD;
	}
	public Resource getTdbResource() {
		return tdbResource;
	}
	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}
	public void addFileMD(FileMD fileMD){
		fileMDList.add(fileMD);
	}
	public List<FileMD> getFileMDList() {
		return fileMDList;
	}
}
