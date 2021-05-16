package com.smallaswater.npc.route;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.entitys.EntityRsNpc;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    private final int distance;
    private Entity entity;
    
    LinkedList<Node> nodes = new LinkedList<>();
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end) {
        this(level, start, end, null);
    }
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end, Entity entity) {
        this.level = level;
        this.start = start.floor();
        this.end = end.floor();
    
        this.distance = (int) start.distance(end);
        
        this.entity = entity;
        if (this.entity == null) {
            Position position = Position.fromObject(start, level);
            this.entity = new EntityHuman(position.getChunk(),
                    Entity.getDefaultNBT(position).putCompound("Skin", new CompoundTag()));
        }
    
        Server.getInstance().getScheduler().scheduleAsyncTask(RsNpcX.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                process();
                if (entity instanceof EntityRsNpc) {
                    ((EntityRsNpc) entity).getNodes().addAll(nodes);
                    ((EntityRsNpc) entity).setLockRoute(false);
                }
                //TODO
                show();
            }
        });
    }
    
    private void process() {
        LinkedList<Node> needChecks = new LinkedList<>();
        LinkedList<Node> needChecksLow = new LinkedList<>(); //低优先级
        ArrayList<Vector3> completeList = new ArrayList<>();
        needChecks.add(new Node(this.start));
        
        Node nowNode;
        while ((nowNode = needChecks.poll()) != null || (nowNode = needChecksLow.poll()) != null) {
            //到达终点，保存路径
            if (nowNode.getVector3().distance(this.getEnd()) < 0.5) {
                Node parent = nowNode;
                parent.setVector3(parent.getVector3().add(0.5, 0, 0.5));
                this.nodes.add(parent);
                while ((parent = parent.getParent()) != null) {
                    parent.setVector3(parent.getVector3().add(0.5, 0, 0.5));
                    this.nodes.addFirst(parent);
                }
                break;
            }
    
            LinkedList<Node> nextNodes = new LinkedList<>();
    
            for (int y = -1; y <= 1; y++) {
                this.check(nowNode, nextNodes, completeList, 0, y, 1);
                this.check(nowNode, nextNodes, completeList, 1, y, 0);
                this.check(nowNode, nextNodes, completeList, 0, y, -1);
                this.check(nowNode, nextNodes, completeList, -1, y, 0);
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
                return d1 > d2 ? -1 : 1;
            });
            
            
            needChecks.add(nextNodes.pollLast());
            for (Node node : nextNodes) {
                needChecksLow.addFirst(node);
            }
            needChecksLow.sort((o1, o2) -> {
                double d1 = o1.getVector3().distance(this.getEnd());
                double d2 = o2.getVector3().distance(this.getEnd());
                if (d1 == d2) {
                    return 0;
                }
                return d1 > d2 ? 1 : -1;
            });
            
            //TODO 寻路失败时跳出
            /*if (completeList.size() > this.getDistance() * this.getDistance() * Math.abs(this.getStart().getY() - this.getEnd().getY())) {
                break;
            }*/
        }
        
        this.processingComplete = true;
    }
    
    private void check(Node nowNode, LinkedList<Node> nextNodes, ArrayList<Vector3> completeList, int x, int y, int z) {
        Node nextNode = new Node(nowNode.getVector3().add(x, y, z), nowNode);
        if (completeList.contains(nextNode.getVector3())) {
            return;
        }
        completeList.add(nextNode.getVector3());
        if (this.canMoveTo(nowNode, nextNode)) {
            nextNodes.add(nextNode);
        }
    }
    
    /**
     * 检查是否可以移动到目标节点
     *
     * @param nowNode 当前节点
     * @param target 目标节点
     * @return 是否可以移动到目标节点
     */
    private boolean canMoveTo(Node nowNode, Node target) {
        if (!this.getLevel().getBlock(target.getVector3()).canPassThrough() ||
                !this.getLevel().getBlock(target.getVector3().add(0, 1, 0)).canPassThrough() ||
                !this.getLevel().getBlock(target.getVector3().add(0, -1, 0)).isNormalBlock()) {
            return false;
        }
        
        //跳跃检查
        if (target.getVector3().getY() > nowNode.getVector3().getY() &&
                !this.getLevel().getBlock(nowNode.getVector3().add(0, 2, 0)).canPassThrough()) {
            return false;
        }
        
        if (target.getVector3().getY() < nowNode.getVector3().getY() &&
                !this.getLevel().getBlock(target.getVector3().add(0, 2, 0)).canPassThrough()) {
            return false;
        }
        
        return true;
    }
    
    public void show() {
        Server.getInstance().getScheduler().scheduleAsyncTask(RsNpcX.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                for (Node node : nodes) {
                    level.addParticleEffect(node.getVector3(), ParticleEffect.REDSTONE_TORCH_DUST);
                    level.addParticleEffect(node.getVector3().add(0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
                    level.addParticleEffect(node.getVector3().add(0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
                    level.addParticleEffect(node.getVector3().add(-0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
                    level.addParticleEffect(node.getVector3().add(-0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
                }
            }
        });
    }
    
}
