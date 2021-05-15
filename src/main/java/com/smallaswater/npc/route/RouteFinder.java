package com.smallaswater.npc.route;

import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;
import com.smallaswater.npc.RsNpcX;
import lombok.Getter;

import java.util.LinkedList;

/**
 * @author lt_name
 */
@Getter
public class RouteFinder {
    
    private boolean processingComplete = false;
    
    private final Level level;
    private final Vector3 start;
    private final Vector3 end;
    
    LinkedList<Node> nodes = new LinkedList<>();
    
    public RouteFinder(Level level, Vector3 start, Vector3 end) {
        this.level = level;
        this.start = start;
        this.end = end;
    
        Server.getInstance().getScheduler().scheduleAsyncTask(RsNpcX.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                process();
            }
        });
        
    }
    
    private void process() {
        LinkedList<Node> needChecks = new LinkedList<>();
        LinkedList<Node> needChecksLow = new LinkedList<>(); //低优先级
        needChecks.add(new Node(this.start));
        
        Node nowNode;
        while ((nowNode = needChecks.poll()) != null || (nowNode = needChecksLow.poll()) != null) {
            if (nowNode.getVector3().distance(this.getEnd()) < 0.5) {
                Node parent = nowNode;
                this.nodes.add(parent);
                while ((parent = parent.getParent()) != null) {
                    this.nodes.addFirst(parent);
                }
                break;
            }
    
            LinkedList<Node> nextNodes = new LinkedList<>();
    
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        Node nextNode = new Node(nowNode.getVector3().add(x, y, z), nowNode);
                        if (this.canMoveTo(nowNode, nextNode)) {
                            nextNodes.add(nextNode);
                        }
                    }
                }
            }
    
            if (nextNodes.isEmpty()) {
                continue;
            }
            
            nextNodes.sort((o1, o2) -> {
                double d1 = o1.getVector3().distance(this.getEnd());
                double d2 = o2.getVector3().distance(this.getEnd());
                if (d1 == d2) {
                    return 0;
                }
                return d1 > d2 ? 1 : -1;
            });
            
            
            needChecks.add(nextNodes.poll());
            for (Node node : nextNodes) {
                needChecksLow.addFirst(node);
            }
    
            Server.getInstance().getLogger().info("=============================================");
            Server.getInstance().getLogger().info("needChecks size:" + needChecks.size());
            Server.getInstance().getLogger().info("needChecksLow size:" + needChecksLow.size());
            Server.getInstance().getLogger().info("=============================================");
    
        }
        
        this.processingComplete = true;
    }
    
    /**
     * 检查是否可以移动到目标节点
     *
     * @param nowNode 当前节点
     * @param target 目标节点
     * @return 是否可以移动到目标节点
     */
    private boolean canMoveTo(Node nowNode, Node target) {
        if (this.getLevel().getBlock(target.getVector3()).getId() != BlockID.AIR ||
                !this.getLevel().getBlock(target.getVector3().add(0, -1, 0)).isNormalBlock()) {
            return false;
        }
        
        //跳跃检查
        if (target.getVector3().getY() > nowNode.getVector3().getY() &&
                this.getLevel().getBlock(nowNode.getVector3().add(0, 2, 0)).getId() != BlockID.AIR) {
            return false;
        }
        
        return this.getLevel().getBlock(target.getVector3().add(0, 1, 0)).getId() == BlockID.AIR;
    }
    
    
}
