package vfcorp;

public class EpicorParser {

	private TLOG tlog;
	private RPC rpc;
	
	public EpicorParser() {
		tlog = new TLOG();
		rpc = new RPC();
	}
	
	public TLOG tlog() {
		return tlog;
	}
	
	public RPC rpc() {
		return rpc;
	}
}
