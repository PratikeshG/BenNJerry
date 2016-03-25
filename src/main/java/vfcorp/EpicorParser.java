package vfcorp;

public class EpicorParser {

	private TLOG tlog;
	private RPCParser rpc;
	
	public EpicorParser() {
		tlog = new TLOG();
		rpc = new RPCParser();
	}
	
	public TLOG tlog() {
		return tlog;
	}
	
	public RPCParser rpc() {
		return rpc;
	}
}
