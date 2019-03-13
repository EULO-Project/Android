package global;

import java.util.ArrayList;
import java.util.List;

import pivtrum.UloNodeData;
import wallet.rate.db.NodeDb;

/**
 * Created by furszy on 7/2/17.
 */

public class PivtrumGlobalData {


    private static PivtrumGlobalData instance = new PivtrumGlobalData();

//    public static final String[] TRUSTED_NODES = new String[]{"node.pivxwiki.org", "panther.pivxwiki.org", "pivx.warrows.fr"};

//    public static final String[] TRUSTED_NODES = new String[]{"47.74.14.246", "47.89.243.161"};

    public List<UloNodeData> listTrustedHosts() {
        List<UloNodeData> list = new ArrayList<>();
//        for (String trustedNode : TRUSTED_NODES) {
////            list.add(new UloNodeData(trustedNode,51472,55552));
//            list.add(new UloNodeData(trustedNode,58802,55552));
//        }
        list.add(getDefultTrustedHost());

        if (nodeDb != null) {
            try {
                list.addAll(nodeDb.list());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return list;
    }

    public NodeDb getNodeDb() {
        return nodeDb;
    }

    public void setNodeDb(NodeDb nodeDb) {
        this.nodeDb = nodeDb;
    }

    private NodeDb nodeDb;

    private PivtrumGlobalData() {
    }

    public static final UloNodeData getDefultTrustedHost() {
        return new UloNodeData("seed1.eulo.io", 58802);
    }


    public static PivtrumGlobalData getInstance() {
        return instance;
    }
}
