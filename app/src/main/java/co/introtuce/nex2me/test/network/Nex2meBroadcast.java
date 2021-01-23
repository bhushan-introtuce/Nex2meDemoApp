package co.introtuce.nex2me.test.network;




public class Nex2meBroadcast {
	

	private String id;
	private String hls;
	private RTCSession session;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHls() {
		return hls;
	}
	public void setHls(String hls) {
		this.hls = hls;
	}
	public RTCSession getSession() {
		return session;
	}
	public void setSession(RTCSession session) {
		this.session = session;
	}

}
