package pivtrum;

/**
 * Created by furszy on 6/13/17.
 */

public class UloNodeData {

    private String host;
    private int tcpPort;
//    private int sslPort;
    private long prunningLimit;

    public UloNodeData(String host, int tcpPort) {
        this.host = host;
        this.tcpPort = tcpPort;
//        this.sslPort = sslPort;
    }

    public String getHost() {
        return host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

//    public int getSslPort() {
//        return sslPort;
//    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UloNodeData)) return false;
        UloNodeData other = (UloNodeData) o;
        if (!this.host.equals(other.getHost())){
            return false;
        }
        if (this.tcpPort != other.tcpPort){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UloNodeData{" +
                "host='" + host + '\'' +
                ", tcpPort=" + tcpPort +
//                ", sslPort=" + sslPort +
                ", prunningLimit=" + prunningLimit +
                '}';
    }
}
